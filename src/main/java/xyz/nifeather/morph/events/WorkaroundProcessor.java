package xyz.nifeather.morph.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.api.events.gameplay.PlayerMorphEarlyEvent;

import java.util.function.Predicate;

public class WorkaroundProcessor extends MorphPluginObject implements Listener
{
    /**
     * Prevent players locking others by disguising as Creaking
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMorph(PlayerMorphEarlyEvent event)
    {
        var player = event.getPlayer();

        if (!event.getTargetId().equals(EntityType.CREAKING.key().asString()))
            return;

        var passengers = player.getPassengers();
        if (passengers.isEmpty())
            return;

        passengers.forEach(player::removePassenger);
    }

    /**
     * Prevents player sitting on another player that disguised as Creaking
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event)
    {
        if (!(event.getEntity() instanceof Player player))
            return;

        var morphManager = FeatherMorphAPI.instance().directAccess().morphManager();

        var playerDisguiseState = morphManager.getDisguiseStateFor(player);

        if (playerDisguiseState != null)
        {
            if (playerDisguiseState.getEntityType() == EntityType.PLAYER)
                playerDisguiseState.stopAnimations();
        }

        // Riding on a virtual Creaking would result in a bad state for game clients
        var matchingVehicle = findAnyVehicle(event.getMount(), entity ->
        {
            if (!(entity instanceof Player vehiclePlayer))
                return false;

            var theirState = morphManager.getDisguiseStateFor(vehiclePlayer);
            if (theirState == null)
                return false;

            return theirState.getEntityType() == EntityType.CREAKING;
        });

        if (matchingVehicle == null)
            return;

        final var mountLocation = player.getLocation();

        // Bad behavior! We should cancel the event...
        // ...but having `HIGHEST` priority and cancelling the event would cause a bad state for GSit... :<
        this.scheduleOn(player, () ->
        {
            if (player.getVehicle() != event.getMount())
                return;

            player.leaveVehicle();
            player.teleportAsync(mountLocation);
        });

        event.setCancelled(true);
    }

    @Nullable
    private Entity findAnyVehicle(Entity topVehicle, Predicate<Entity> predicate)
    {
        if (topVehicle == null) return null;

        if (predicate.test(topVehicle))
            return topVehicle;

        return findAnyVehicle(topVehicle.getVehicle(), predicate);
    }
}
