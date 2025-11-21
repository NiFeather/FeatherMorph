package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;

public class LateDisguiseBuildEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() { return handlers; }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    private final DisguiseState buildingState;

    public DisguiseState buildingState()
    {
        return buildingState;
    }

    public LateDisguiseBuildEvent(Player player, DisguiseState buildingState)
    {
        super(player);

        this.buildingState = buildingState;
    }
}
