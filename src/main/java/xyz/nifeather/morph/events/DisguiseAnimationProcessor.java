package xyz.nifeather.morph.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Resolved;

public class DisguiseAnimationProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private PlayerTracker tracker;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        var player = e.getPlayer();
        if (e.getAction().isLeftClick() && !tracker.isBreakingSuspect(player))
        {
            var state = morphManager.getDisguiseStateFor(player);
            if (state != null)
                state.getDisguiseWrapper().playAttackAnimation();
        }
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e)
    {
        var state = morphManager.getDisguiseStateFor(e.getDamager());
        if (state == null) return;

        state.getDisguiseWrapper().playAttackAnimation();
    }
}
