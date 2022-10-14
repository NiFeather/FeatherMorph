package xiamomc.morph.storage.playerdata;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.*;

public class PlayerDataManager extends MorphJsonBasedStorage<MorphConfiguration> implements IManagePlayerData
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
    protected @NotNull MorphConfiguration createDefault()
    {
        return new MorphConfiguration();
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

                //先对原始列表排序
                Collections.sort(c.unlockedDisguiseIdentifiers);

                //然后逐个添加
                c.unlockedDisguiseIdentifiers.forEach(i ->
                {
                    var type = EntityTypeUtils.fromString(i);

                    if (type != null)
                    {
                        if (type.equals(EntityType.PLAYER))
                            list.add(new DisguiseInfo(i.replace("player:", "")));
                        else
                            list.add(new DisguiseInfo(type));
                    }
                    else
                        Logger.warn("未能找到和\"" + i + "\"对应的实体类型，将不会添加到" + c.playerName + "(" + c.uniqueId + ")的列表中");
                });

                //设置可用的伪装列表
                c.unlockedDisguises = list;
            });

            saveConfiguration();
        }

        return success;
    }

    private final int targetConfigurationVersion = 3;

    private void migrate(MorphConfiguration configuration)
    {
        //1 -> 2
        if (configuration.Version == 1)
            configuration.playerMorphConfigurations.forEach(c ->
            {
                 if (Objects.equals(c.playerName, "Unknown")) c.playerName = null;
            });

        //2 -> 3
        if (configuration.Version == 2)
          configuration.playerMorphConfigurations.forEach(c ->
          {
              //新建ID列表
              var list = new ArrayList<String>();

              //遍历Info
              c.unlockedDisguises.forEach(i ->
              {
                  //跳过无效配置
                  if (i.type == null)
                  {
                      Logger.warn("发现无效的Info配置: " + i);
                      return;
                  }

                  //检查是否为玩家伪装
                  if (i.isPlayerDisguise())
                      list.add("player:" + i.playerDisguiseTargetName);
                  else
                      list.add(i.type.getKey().asString());
              });

              //设置配置的ID列表
              c.unlockedDisguiseIdentifiers = list;

              //移除配置原有的伪装列表
              c.unlockedDisguises = null;
          });

        //migrate完设置版本
        configuration.Version = targetConfigurationVersion;
    }

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        var valueOptional = storingObject.playerMorphConfigurations
                .stream().filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst();

        if (valueOptional.isPresent())
        {
            var value = valueOptional.get();
            if (value.playerName == null) value.playerName = player.getName();

            return value;
        }
        else
        {
            var newInstance = new PlayerMorphConfiguration();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.playerName = player.getName();
            newInstance.unlockedDisguiseIdentifiers = new ArrayList<>();

            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.commandHintString()));

            storingObject.playerMorphConfigurations.add(newInstance);
            return newInstance;
        }
    }

    @Override
    public boolean grantMorphToPlayer(Player player, EntityType type)
    {
        var playerConfiguration = getPlayerConfiguration(player);
        var info = getDisguiseInfo(type);

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(info)))
        {
            playerConfiguration.addDisguise(info);
            saveConfiguration();
        }
        else return false;

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphUnlockedString()
                        .resolve("what", Component.translatable(type.translationKey()))
                        .toComponent());

        return true;
    }

    @Override
    public boolean grantPlayerMorphToPlayer(Player sourcePlayer, String targetPlayerName)
    {
        var playerConfiguration = getPlayerConfiguration(sourcePlayer);

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(targetPlayerName)))
            playerConfiguration.addDisguise(this.getDisguiseInfo(targetPlayerName));
        else
            return false;

        saveConfiguration();

        sendMorphAcquiredNotification(sourcePlayer, morphs.getDisguiseStateFor(sourcePlayer),
                MorphStrings.morphUnlockedString()
                        .resolve("what",targetPlayerName)
                        .toComponent());

        return true;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, EntityType entityType)
    {
        var avaliableDisguises = getAvaliableDisguisesFor(player);

        var optional = avaliableDisguises.stream().filter(d -> d.type == entityType).findFirst();
        if (optional.isEmpty()) return false;

        getPlayerConfiguration(player).removeDisguise(optional.get());
        saveConfiguration();

        var state = morphs.getDisguiseStateFor(player);
        if (state != null && state.getDisguise().getType().getEntityType().equals(entityType))
            morphs.unMorph(player);

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphLockedString()
                        .resolve("what", Component.translatable(entityType.translationKey()))
                        .toComponent());

        return true;
    }

    @Override
    public boolean revokePlayerMorphFromPlayer(Player player, String playerName)
    {
        var avaliableDisguises = getAvaliableDisguisesFor(player);

        var optional = avaliableDisguises.stream()
                .filter(d -> (d.isPlayerDisguise() && Objects.equals(d.playerDisguiseTargetName, playerName))).findFirst();

        if (optional.isEmpty()) return false;

        getPlayerConfiguration(player).removeDisguise(optional.get());
        saveConfiguration();

        var state = morphs.getDisguiseStateFor(player);

        if (state != null
                && state.getDisguise().isPlayerDisguise()
                && ((PlayerDisguise)state.getDisguise()).getName().equals(playerName))
        {
            morphs.unMorph(player);
        }

        sendMorphAcquiredNotification(player, morphs.getDisguiseStateFor(player),
                MorphStrings.morphLockedString()
                        .resolve("what", playerName)
                        .toComponent());

        return true;
    }

    @Override
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        if (type.equals(EntityType.PLAYER)) throw new IllegalArgumentException("玩家不能作为类型传入");

        if (this.cachedInfos.stream().noneMatch(o -> o.equals(type)))
            cachedInfos.add(new DisguiseInfo(type));

        return cachedInfos.stream().filter(o -> o.equals(type)).findFirst().get();
    }

    @Override
    public DisguiseInfo getDisguiseInfo(String playerName)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(playerName)))
            cachedInfos.add(new DisguiseInfo(playerName));

        return cachedInfos.stream().filter(o -> o.equals(playerName)).findFirst().get();
    }

    @Override
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return new ArrayList<>(getPlayerConfiguration(player).unlockedDisguises);
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
