package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMorphEarlyEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private final Map<String, String> propertyInputs = new ConcurrentHashMap<>();

    public static HandlerList getHandlerList() { return handlers; }

    public final String targetId;

    public final boolean isForce;

    /**
     * 会在玩家正式进行伪装或更换伪装前触发
     * @param who 玩家
     * @param isForce 此操作是否为强制执行，若为true则无法取消
     */
    public PlayerMorphEarlyEvent(@NotNull Player who,
                                 @NotNull String targetId,
                                 boolean isForce,
                                 Map<String, String> propertyInputs)
    {
        super(who);

        this.targetId = targetId;
        this.isForce = isForce;
        this.propertyInputs.putAll(propertyInputs);
    }

    /**
     * @return The target disguise ID
     */
    public @NotNull String getTargetId()
    {
        return targetId;
    }

    /**
     * Gets the player's current {@link DisguiseState}, if the player is disguised at this moment
     * @deprecated We no longer include current state in this event
     */
    @Deprecated(forRemoval = true)
    public @Nullable DisguiseState getState()
    {
        return null;
    }

    /**
     * Gets the player's property inputs, paired with Key <-> Value
     */
    @Unmodifiable
    public Map<String, String> getPropertyInputs()
    {
        return propertyInputs;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

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

