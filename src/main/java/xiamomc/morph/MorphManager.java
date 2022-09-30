package xiamomc.morph;

import com.google.gson.Gson;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.MorphConfiguration;
import xiamomc.morph.misc.PlayerMorphConfiguration;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.PluginObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MorphManager extends PluginObject
{
    /**
     * 变成其他玩家的玩家
     * 因为插件限制，需要每tick更新下蹲和疾跑状态
     */
    private final List<Player> disguisedPlayers = new ArrayList<>();

    private final List<DisguiseInfo> cachedInfos = new ArrayList<>();

    private MorphConfiguration morphConfiguration;

    private final Gson gson = new Gson();

    @Resolved
    private MorphPlugin plugin;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        //加载配置
        MorphConfiguration targetConfiguration = null;
        try ( var jsonStream = new InputStreamReader(new FileInputStream(configurationFile)) )
        {
            targetConfiguration = gson.fromJson(jsonStream, MorphConfiguration.class);
        }
        catch (IOException ignored)
        {
            targetConfiguration = null;
            Logger.warn("配置加载失败");
        }

        if (targetConfiguration == null) targetConfiguration = new MorphConfiguration();

        morphConfiguration = targetConfiguration;
        saveConfiguration();
    }

    private final File configurationFile = new File("/dev/shm/test");

    private void saveConfiguration()
    {
        try
        {
            var jsonString = gson.toJson(morphConfiguration);

            if (configurationFile.exists()) configurationFile.delete();

            configurationFile.createNewFile();

            try ( var stream = new FileOutputStream(configurationFile) )
            {
                stream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        var valueOptional = morphConfiguration.playerMorphConfigurations
                .stream().filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst();

        if (valueOptional.isPresent()) return valueOptional.get();
        else
        {
            var newInstance = new PlayerMorphConfiguration();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.morphOnKill = false;
            newInstance.unlockedDisguises = new ArrayList<>();

            morphConfiguration.playerMorphConfigurations.add(newInstance);
            return newInstance;
        }
    }

    public void addNewMorphToPlayer(Player player, Entity entity)
    {
        var playerConfiguration = getPlayerConfiguration(player);
        var info = this.getDisguiseInfo(entity.getType());

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(info)))
        {
            playerConfiguration.unlockedDisguises.add(info);
            saveConfiguration();
        }
        else return;

        player.sendMessage(Component.text("你获得了")
                .append(Component.translatable(entity.getType().translationKey()))
                .append(Component.text("的伪装！")));
    }

    public void addNewPlayerMorphToPlayer(Player sourcePlayer, Player targtPlayer)
    {
        var playerConfiguration = getPlayerConfiguration(sourcePlayer);

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(targtPlayer.getName())))
            playerConfiguration.unlockedDisguises.add(this.getDisguiseInfo(targtPlayer));

        sourcePlayer.sendMessage(Component.text("已解锁" + targtPlayer.getName() + "的伪装！"));

        saveConfiguration();
    }

    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerConfiguration(player).unlockedDisguises;
    }

    private void update()
    {
        var disguisers = new ArrayList<>(disguisedPlayers);
        for (var p : disguisers)
        {
            var disg = DisguiseAPI.getDisguise(p);
            if (disg == null || !disg.getType().isPlayer() || !p.isOnline())
            {
                disguisedPlayers.remove(p);
                continue;
            }

            var watcher = disg.getWatcher();
            watcher.setSneaking(p.isSneaking() && !p.isFlying());
            watcher.setFlyingWithElytra(p.isGliding());
            watcher.setSprinting(p.isSprinting());
        }

        this.addSchedule(c -> update());
    }

    //缓存伪装信息
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(type)))
            cachedInfos.add(new DisguiseInfo(type));

        return cachedInfos.stream().filter(o -> o.equals(type)).findFirst().get();
    }

    public DisguiseInfo getDisguiseInfo(Player player)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(player.getName())))
            cachedInfos.add(new DisguiseInfo(player.getName()));

        return cachedInfos.stream().filter(o -> o.equals(player.getName())).findFirst().get();
    }

    //region 玩家伪装相关

    /**
     * 将玩家伪装成指定的实体类型
     * @param player 目标玩家
     * @param entityType 目标实体类型
     */
    public void morph(Player player, EntityType entityType)
    {
        var targetedEntity = player.getTargetEntity(3);
        Disguise constructedDisguise = null;

        //如果正在看的实体和目标伪装类型一样，那么优先采用
        if (targetedEntity != null && targetedEntity.getType() == entityType && !targetedEntity.isDead())
        {
            constructedDisguise = DisguiseAPI.constructDisguise(targetedEntity, true, false);
        }
        else //如果没有，则正常构建
        {
            targetedEntity = null;

            //不要构建玩家类型的伪装
            if (entityType == EntityType.PLAYER) return;

            constructedDisguise = new MobDisguise(DisguiseType.getType(entityType));
        }

        postConstructDisguise(player, targetedEntity, constructedDisguise);

        DisguiseAPI.disguiseEntity(player, constructedDisguise);
    }

    /**
     * 将玩家伪装成指定的实体
     * @param sourcePlayer 目标玩家
     * @param entity 目标实体
     */
    public void morph(Player sourcePlayer, Entity entity)
    {
        Disguise constructedDisguise = null;

        //检查目标实体有没有伪装
        if (DisguiseAPI.isDisguised(entity))
            constructedDisguise = DisguiseAPI.getDisguise(entity);
        else
            constructedDisguise = DisguiseAPI.constructDisguise(entity);

        postConstructDisguise(sourcePlayer, entity, constructedDisguise);

        DisguiseAPI.disguiseEntity(sourcePlayer, constructedDisguise);
    }

    public void morph(Player sourcePlayer, String targetPlayerName)
    {
        var disguise = new PlayerDisguise(targetPlayerName);

        postConstructDisguise(sourcePlayer, null, disguise);

        DisguiseAPI.disguiseEntity(sourcePlayer, disguise);
    }

    private void postConstructDisguise(Player sourcePlayer, Entity targetEntity, Disguise disguise)
    {
        var watcher = disguise.getWatcher();

        watcher.setInvisible(false);

        if (targetEntity instanceof LivingEntity living && living.getHealth() <= 0)
            ((LivingWatcher)watcher).setHealth(1);

        if (targetEntity instanceof Player targetPlayer)
        {
            var offHandItemStack = targetPlayer.getInventory().getItemInOffHand();

            watcher.setItemInOffHand(offHandItemStack);
        }

        if (disguise.isPlayerDisguise())
            disguisedPlayers.add(sourcePlayer);
    }

    //endregion 玩家伪装相关

    private Key entityTypeToNameSpacedID(EntityType type)
    {
        return type.getKey();
    }

    private EntityType nameSpacedIDToEntityType(Key key)
    {
        return EntityType.valueOf(key.value());
    }
}
