package xiamomc.morph.storage.playerdata;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PlayerDataManager extends MorphJsonBasedStorage<PlayerMorphConfigurationContainer> implements IManagePlayerData
{
    private final List<DisguiseInfo> cachedInfos = new ArrayList<>();

    @Resolved
    private MorphManager morphs;

    @Override
    protected @NotNull String getFileName()
    {
        return "data.json";
    }

    @Override
    protected @NotNull PlayerMorphConfigurationContainer createDefault()
    {
        return new PlayerMorphConfigurationContainer();
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

        if (success)
        {
            if (storingObject.Version < targetConfigurationVersion)
                migrate(storingObject);

            storingObject.playerMorphConfigurations.forEach(c ->
            {
                //要设置给c.unlockedDisguises的列表
                var list = new ArrayList<DisguiseInfo>();

                //原始列表
                var unlockedDisguiseIdentifiers = c.getUnlockedDisguiseIdentifiers();

                //先对原始列表排序
                Collections.sort(unlockedDisguiseIdentifiers);

                //然后逐个添加
                unlockedDisguiseIdentifiers.forEach(i ->
                {
                    var type = DisguiseTypes.fromId(i);

                    if (type != null)
                        list.add(new DisguiseInfo(i, DisguiseTypes.fromId(i)));
                    else
                        logger.warn("未能找到和\"" + i + "\"对应的实体类型，将不会添加到" + c.playerName + "(" + c.uniqueId + ")的列表中");
                });

                //设置可用的伪装列表并对其加锁
                c.setUnlockedDisguises(list);
                c.lockDisguiseList();
            });
        }

        return success;
    }

    private final int targetConfigurationVersion = 3;

    private void migrate(PlayerMorphConfigurationContainer configuration)
    {
        //1 -> 2: 玩家名处理
        if (configuration.Version == 1)
            configuration.playerMorphConfigurations.forEach(c ->
            {
                 if (Objects.equals(c.playerName, "Unknown")) c.playerName = null;
            });

        //2 -> 3: 从存储EntityType变为存储ID
        if (configuration.Version == 2)
          configuration.playerMorphConfigurations.forEach(c ->
          {
              //新建ID列表
              var list = new ArrayList<String>();

              //遍历Info
              c.getUnlockedDisguises().forEach(i ->
              {
                  //跳过无效配置
                  if (i.getDisguiseType() == DisguiseTypes.UNKNOWN)
                  {
                      logger.warn("发现无效的Info配置: " + i);
                      return;
                  }

                  list.add(i.getKey());
              });

              //设置配置的ID列表
              c.setUnlockedDisguiseIdentifiers(list);

              //移除配置原有的伪装列表
              c.setUnlockedDisguises(null);
          });

        //migrate完设置版本
        configuration.Version = targetConfigurationVersion;
    }

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        var value = storingObject.playerMorphConfigurations
                .stream().filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst().orElse(null);

        if (value != null)
        {
            if (value.playerName == null) value.playerName = player.getName();

            return value;
        }
        else
        {
            var newInstance = new PlayerMorphConfiguration();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.playerName = player.getName();

            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.commandHintString()));

            storingObject.playerMorphConfigurations.add(newInstance);
            return newInstance;
        }
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        var playerConfiguration = getPlayerConfiguration(player);
        var info = getDisguiseInfo(disguiseIdentifier);

        if (info == null) return false;

        if (playerConfiguration.getUnlockedDisguises().stream().noneMatch(c -> c.equals(info)))
        {
            playerConfiguration.addDisguise(info);
            saveConfiguration();
        }
        else return false;

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphUnlockedString()
                        .resolve("what", info.asComponent())
                        .toComponent());

        return true;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        var avaliableDisguises = getAvaliableDisguisesFor(player);

        var info = avaliableDisguises.stream().filter(d -> d.equals(disguiseIdentifier)).findFirst().orElse(null);
        if (info == null) return false;

        getPlayerConfiguration(player).removeDisguise(info);
        saveConfiguration();

        var state = morphs.getDisguiseStateFor(player);
        if (state != null && info.getKey().equals(state.getDisguiseIdentifier()))
            morphs.unMorph(player);

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphLockedString()
                        .resolve("what", info.asComponent())
                        .toComponent());

        return true;
    }

    @Override
    @Nullable
    public DisguiseInfo getDisguiseInfo(String rawString)
    {
       var type = DisguiseTypes.fromId(rawString);

        if (this.cachedInfos.stream().noneMatch(o -> o.equals(rawString)))
            cachedInfos.add(new DisguiseInfo(rawString, type));

        return cachedInfos.stream().filter(o -> o.equals(rawString)).findFirst().orElse(null);
    }

    @Override
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerConfiguration(player).getUnlockedDisguises();
    }

    //endregion Implementation of IManagePlayerData

    private void sendMorphAcquiredNotification(Player player, @Nullable DisguiseState state, Component text)
    {
        if (state == null)
            player.sendActionBar(text);
        else
            player.sendMessage(MessageUtils.prefixes(player, text));
    }
}
