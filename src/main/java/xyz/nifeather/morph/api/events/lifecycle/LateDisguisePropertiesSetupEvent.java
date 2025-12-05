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
 * Called after properties for the bindingState finished parsing,
 * before the {@link xyz.nifeather.morph.providers.disguise.DisguiseProvider#finalizeProperties(DisguiseState)} is called.
 * <br>
 * You can use this event to validate properties in the given {@link DisguiseState}.
 *
 * @apiNote Only when this event is cancelled, or FeatherMorph will continue the disguise process.<br>
 *          To prevent FeatherMorph from continuing disguise process, use {@link LateDisguisePropertiesSetupEvent#report(Exception)} to report an exception (and automatically cancel the event), or {@link LateDisguisePropertiesSetupEvent#setCancelled(boolean)} to cancel the event (not suggested)
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
     * Report an exception and cancel this event. FeatherMorph would check if an exception was reported and abort the process.
     * <br>
     * To report a user fault in inputs, use {@link xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException#forProperty(String)}.
     * <br>
     * If any property doesn't seem right, use {@link xyz.nifeather.morph.misc.disguiseProperty.PropertyValidationException#forProperty(String)}
     * <br>
     * Any other exceptions are still accepted.
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
