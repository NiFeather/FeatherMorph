package xyz.nifeather.morph.api.events.lifecycle;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.events.AbstractDisguiseSessionRelatedEvent;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;

/**
 * Called after properties for the bindingState finished parsing, before the {@link xyz.nifeather.morph.providers.disguise.DisguiseProvider#finalizeProperties(DisguiseState)} is called
 */
public class LateDisguisePropertiesSetupEvent extends AbstractDisguiseSessionRelatedEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() { return handlers; }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public PropertyHandler propertyHandler()
    {
        return bindingState().disguisePropertyHandler();
    }

    @Nullable
    private Exception throwable;

    /**
     * Report an exception and cancel this event. FeatherMorph would check if an exception was reported and abort the process
     */
    public void report(Exception e)
    {
        if (throwable == null)
        {
            throwable = e;
            setCancelled(true);
        }
    }

    @Nullable
    public Exception exception()
    {
        return throwable;
    }

    public LateDisguisePropertiesSetupEvent(Player player, DisguiseState bindingState)
    {
        super(player, bindingState);
    }

    private boolean cancelled;

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return {@code true} if this event is cancelled
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
     * @param cancel {@code true} if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }
}
