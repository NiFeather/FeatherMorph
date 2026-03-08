package xyz.nifeather.morph.api.events.lifecycle;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.api.events.AbstractDisguiseSessionRelatedEvent;
import xyz.nifeather.morph.misc.DisguiseState;

public class LateDisguiseBuildEvent extends AbstractDisguiseSessionRelatedEvent
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() { return handlers; }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public LateDisguiseBuildEvent(Player player, DisguiseState buildingState)
    {
        super(player, buildingState);
    }
}
