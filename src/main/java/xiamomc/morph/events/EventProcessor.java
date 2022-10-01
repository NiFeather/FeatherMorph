package xiamomc.morph.events;

import com.comphenix.protocol.PacketType;
import dev.geco.gsit.api.event.*;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.List;

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
    public void onLeave(PlayerQuitEvent e)
    {
        gSitHandlingPlayers.remove(e.getPlayer());
    }

    //region GSit <-> LibsDisguises workaround

    @EventHandler
    public void onEntityGetUp(EntityGetUpSitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            showDisguiseFor(player);
    }

    private final List<Player> gSitHandlingPlayers = new ArrayList<>();

    @EventHandler
    public void onEntitySit(EntitySitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            hideDisguiseFor(player);
    }

    @EventHandler
    public void onPlayerPlayerSit(PlayerPlayerSitEvent e)
    {
        gSitHandlingPlayers.add(e.getPlayer());
        hideDisguiseFor(e.getPlayer());
    }

    @EventHandler
    public void onPlayerGetUpPlayerSit(PlayerGetUpPlayerSitEvent e)
    {
        if (gSitHandlingPlayers.contains(e.getPlayer()))
        {
            showDisguiseFor(e.getPlayer());
            gSitHandlingPlayers.remove(e.getPlayer());
        }
    }

    //endregion  GSit <-> LibsDisguises workaround

    private void hideDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
            DisguiseUtilities.removeSelfDisguise(DisguiseAPI.getDisguise(player));
    }

    private void showDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
          this.addSchedule(c ->
          {
                if (DisguiseAPI.isDisguised(player))
                    DisguiseUtilities.setupFakeDisguise(DisguiseAPI.getDisguise(player));
          });
    }

    @EventHandler()
    public void onPlayerSwapOffhand(PlayerSwapHandItemsEvent e)
    {
        var player = e.getPlayer();
        if (DisguiseAPI.isDisguised(player))
        {
            //workaround: LibsDisguises在启用selfDisguiseVisible的情况下会导致副手切换异常
            this.addSchedule(c ->
            {
                if (DisguiseAPI.isDisguised(player)) player.updateInventory();
            }, 2);
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
