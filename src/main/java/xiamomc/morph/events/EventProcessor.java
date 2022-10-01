package xiamomc.morph.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.TabCompleteEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;

public class EventProcessor extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e)
    {
        var entity = e.getEntity();
        var killer = entity.getKiller();

        var logger = Plugin.getSLF4JLogger();

        //logger.warn(entity + "died by:" + killer);

        //盔甲架需要额外的一些东西
        if (entity.getType() == EntityType.ARMOR_STAND)
        {
            //logger.warn("IS armor stand");
            var lastCause = entity.getLastDamageCause();

            //logger.warn("cause: " + String.valueOf(lastCause));
            if (lastCause instanceof  EntityDamageByEntityEvent damageEvent)
            {
                var cause = damageEvent.getDamager();

                //logger.warn("cause entity: " + cause);
                if (cause instanceof Player) killer = (Player) cause;
            }
        }

        if (killer != null)
            this.onPlayerKillEntity(killer, e.getEntity());
    }

    @Resolved
    private MorphCommandHelper cmdHelper;

    @EventHandler
    public void onTabComplete(TabCompleteEvent e)
    {
        if (e.isCancelled()) return;

        //从buffer获取指令名
        var buffers = e.getBuffer().split(" ");

        var result = cmdHelper.onTabComplete(buffers, e.getSender());
        if (result != null) e.setCompletions(result);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if (e.getRightClicked() instanceof Player targetPlayer)
        {
            var sourcePlayer = e.getPlayer();

            if (sourcePlayer.isSneaking())
            {
                //todo
            }
        }
    }

    @Resolved
    private MorphManager morphs;

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        //Plugin.getSLF4JLogger().warn(entity.getType().toString());
        switch (entity.getType())
        {
            case DROPPED_ITEM:
            case SPLASH_POTION:
            case WITHER_SKULL:
            case SMALL_FIREBALL:
            case FIREBALL:
            case ARROW:
            case FISHING_HOOK:
            case ITEM_FRAME:
            case PAINTING:
                return;

            case PLAYER:
                var targetPlayer = (Player) entity;
                morphs.addNewPlayerMorphToPlayer(player, targetPlayer);
                break;

            default:
                morphs.addNewMorphToPlayer(player, entity);
                break;
        }
    }
}
