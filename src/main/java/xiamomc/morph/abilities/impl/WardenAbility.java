package xiamomc.morph.abilities.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.GenericGameEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;

import java.util.List;

public class WardenAbility extends NoOpOptionAbility
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.WARDEN;
    }

    private final List<GameEvent> blockedEvents = ObjectArrayList.of(
            GameEvent.STEP,
            GameEvent.HIT_GROUND
    );

    @EventHandler
    private void onGenericGameEvent(GenericGameEvent e)
    {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!this.appliedPlayers.contains(player)) return;

        if (blockedEvents.stream().anyMatch(ev -> ev.equals(e.getEvent())))
            e.setCancelled(true);
    }

/*
    public WardenAbility()
    {
        RegisteredListener registeredListener = new RegisteredListener(this, (listener, event) -> onEvent(event), EventPriority.NORMAL, MorphPlugin.getInstance(), false);
        for (HandlerList handler : HandlerList.getHandlerLists())
            handler.register(registeredListener);
    }

    private void onEvent(Event event)
    {
        if (event instanceof ServerTickEndEvent
                || event instanceof EntityMoveEvent
                || event instanceof GenericGameEvent
                || event instanceof HopperInventorySearchEvent
                || event instanceof SlimeWanderEvent
                || event instanceof PreCreatureSpawnEvent
                || event instanceof PlayerNaturallySpawnCreaturesEvent
                || event instanceof ServerTickStartEvent
                || event instanceof EntityPathfindEvent
                || event instanceof BlockPhysicsEvent
                || event instanceof BlockEvent
                || event instanceof EntityCombustEvent
                || event instanceof EntitiesLoadEvent
                || event instanceof ChunkLoadEvent
                || event instanceof ServerCommandEvent
                || event instanceof PlayerChunkLoadEvent
                || event instanceof UnknownCommandEvent
                || event instanceof VehicleUpdateEvent
                || event instanceof PlayerEvent
                || event instanceof EntityAddToWorldEvent
                || event instanceof EntityJumpEvent)
        {
            return;
        }

        logger.info("Event! " + event);
    }
*/
}
