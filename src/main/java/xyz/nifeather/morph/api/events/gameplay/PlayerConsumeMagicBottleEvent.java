package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConsumeMagicBottleEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() { return handlers; }

    private boolean cancelled = false;

    public PlayerConsumeMagicBottleEvent(Player player)
    {
        super(player);
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = true;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }
}
