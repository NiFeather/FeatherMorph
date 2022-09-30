package xiamomc.morph;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.PluginObject;

import java.util.ArrayList;
import java.util.List;

public class MorphUtils extends PluginObject
{
    /**
     * 变成其他玩家的玩家
     * 因为插件限制，需要每tick更新下蹲和疾跑状态
     */
    private final List<Player> disguisedPlayers = new ArrayList<>();

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());
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
            disguisedPlayers.add(sourcePlayer);
        }
    }
}
