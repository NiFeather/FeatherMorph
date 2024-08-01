package xiamomc.morph.storage.playerdata;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Objects;

public class PlayerDataStore extends MorphJsonBasedStorage<PlayerMetaContainer> implements IManagePlayerData
{
    private final List<DisguiseMeta> cachedMetas = new ObjectArrayList<>();

    @Resolved
    private MorphManager morphs;

    @Override
    protected @NotNull String getFileName()
    {
        return "data.json";
    }

    @Override
    protected @NotNull PlayerMetaContainer createDefault()
    {
        return new PlayerMetaContainer();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "数据存储";
    }

    //region Implementation of IManagePlayerData

    @Override
    public boolean reloadConfiguration()
    {
        var success = super.reloadConfiguration();

        if (!success) return false;

        if (storingObject.Version < targetConfigurationVersion)
            migrate(storingObject);

        getAll().forEach(c ->
        {
            //要设置给c.unlockedDisguises的列表
            var list = new ObjectArrayList<DisguiseMeta>();

            //原始列表
            var unlockedDisguiseIdentifiers = c.getUnlockedDisguiseIdentifiers();

            //先对原始列表排序
            unlockedDisguiseIdentifiers.sort(null);

            //然后逐个添加
            unlockedDisguiseIdentifiers.forEach(i ->
            {
                var type = DisguiseTypes.fromId(i);

                if (type != null)
                    list.add(new DisguiseMeta(i, DisguiseTypes.fromId(i)));
                else
                    logger.warn("Unknown entity identifier: " + i);
            });

            //设置可用的伪装列表并对其加锁
            c.setUnlockedDisguises(list);
            c.lockDisguiseList();
        });

        return true;
    }

    private final int targetConfigurationVersion = 4;

    private void migrate(PlayerMetaContainer configuration)
    {
        //1 -> 2: 玩家名处理
        if (configuration.Version < 2)
        {
            configuration.playerMetas.forEach(c ->
            {
                if (Objects.equals(c.playerName, "Unknown")) c.playerName = null;
            });
        }

        //2 -> 3: 从存储EntityType变为存储ID
        if (configuration.Version < 3)
        {
            configuration.playerMetas.forEach(c ->
            {
                //新建ID列表
                var list = new ObjectArrayList<String>();

                //遍历Info
                c.getUnlockedDisguises().forEach(i ->
                {
                    //跳过无效配置
                    if (i.getDisguiseType() == DisguiseTypes.UNKNOWN)
                    {
                        logger.warn("Invalid entity identifier: " + i);
                        return;
                    }

                    list.add(i.getKey());
                });

                //设置配置的ID列表
                c.setUnlockedDisguiseIdentifiers(list);

                //移除配置原有的伪装列表
                c.setUnlockedDisguises(null);
            });
        }

        //3 -> 4: LD的ID从`ld`改为`local`
        if(configuration.Version < 4)
        {
            configuration.playerMetas.forEach(c ->
            {
                var list = new ObjectArrayList<String>();

                c.getUnlockedDisguiseIdentifiers().forEach(s ->
                {
                    if (!s.startsWith("ld:")) list.add(s);
                    else list.add(s.replaceFirst("ld:", "local:"));
                });

                c.setUnlockedDisguiseIdentifiers(list);
            });
        }

        //migrate完设置版本
        configuration.Version = targetConfigurationVersion;
    }

    @Override
    public PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        var value = getAll().stream()
                .filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst().orElse(null);

        if (value != null)
        {
            if (value.playerName == null) value.playerName = player.getName();

            return value;
        }
        else
        {
            var newInstance = new PlayerMeta();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.playerName = player.getName();

            synchronized (this)
            {
                storingObject.playerMetas.add(newInstance);
            }

            return newInstance;
        }
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        var playerConfiguration = getPlayerMeta(player);
        var meta = getDisguiseMeta(disguiseIdentifier);

        if (meta == null) return false;

        if (playerConfiguration.getUnlockedDisguises().stream().noneMatch(c -> c.equals(meta)))
        {
            playerConfiguration.addDisguise(meta);
            saveConfiguration();
        }
        else return false;

        var locale = MessageUtils.getLocale(player);

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphUnlockedString()
                        .withLocale(locale)
                        .resolve("what", meta.asComponent(locale))
                        .toComponent(locale));

        return true;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        var avaliableDisguises = getAvaliableDisguisesFor(player);

        var meta = avaliableDisguises.stream().filter(d -> d.equals(disguiseIdentifier)).findFirst().orElse(null);
        if (meta == null) return false;

        getPlayerMeta(player).removeDisguise(meta);
        saveConfiguration();

        var state = morphs.getDisguiseStateFor(player);
        if (state != null && meta.getKey().equals(state.getDisguiseIdentifier()))
            morphs.unMorph(player, true);

        var locale = MessageUtils.getLocale(player);
        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphLockedString()
                        .resolve("what", meta.asComponent(locale))
                        .toComponent(locale));

        return true;
    }

    @Override
    @Nullable
    public DisguiseMeta getDisguiseMeta(String rawString)
    {
       var type = DisguiseTypes.fromId(rawString);

        if (this.cachedMetas.stream().noneMatch(o -> o.equals(rawString)))
            cachedMetas.add(new DisguiseMeta(rawString, type));

        return cachedMetas.stream().filter(o -> o.equals(rawString)).findFirst().orElse(null);
    }

    @Override
    public ObjectArrayList<DisguiseMeta> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerMeta(player).getUnlockedDisguises();
    }

    //endregion Implementation of IManagePlayerData

    private void sendMorphAcquiredNotification(Player player, @Nullable DisguiseState state, Component text)
    {
        if (state == null)
            player.sendActionBar(text);
        else
            player.sendMessage(MessageUtils.prefixes(player, text));
    }

    public synchronized List<PlayerMeta> getAll()
    {
        return new ObjectArrayList<>(storingObject.playerMetas);
    }
}
