package xiamomc.morph.events.api.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseState;

public class PlayerMorphEarlyEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public final DisguiseState state;

    public final String targetId;

    /**
     * 会在玩家正式进行伪装或更换伪装前触发
     * @param who 玩家
     * @param state 玩家的{@link DisguiseState}
     */
    public PlayerMorphEarlyEvent(@NotNull Player who, @Nullable DisguiseState state, @NotNull String targetId)
    {
        super(who);

        this.targetId = targetId;
        this.state = state;
    }

    public @NotNull String getTargetId()
    {
        return targetId;
    }

    public @Nullable DisguiseState getState()
    {
        return state;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }

    private boolean cancelled = false;

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }
}

