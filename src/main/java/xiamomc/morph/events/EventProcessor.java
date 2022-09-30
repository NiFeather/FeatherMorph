package xiamomc.morph.events;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.utils.DisguiseCloneCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.PluginObject;

public class EventProcessor extends PluginObject implements Listener
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
        {
            this.onPlayerKillEntity(killer, e.getEntity());
        }
    }

    @Resolved
    private MorphUtils morphUtils;

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        Plugin.getSLF4JLogger().warn(entity.getType().toString());
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
                morphUtils.morph(player, targetPlayer);

            default:
                morphUtils.morph(player, entity);
                break;
        }
    }
}
