package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.api.events.AbstractDisguiseSessionRelatedEvent;
import xyz.nifeather.morph.misc.DisguiseState;

/**
 * Called when a player undisguises themselves.
 * <br>
 * @apiNote To prevent this action, see {@link PlayerUnMorphEarlyEvent}<br>
 *          <b>This event is not called when a player switches the disguise, for such event, see {@link PlayerSwitchMorphEvent}</b>
 */
public class PlayerUnMorphEvent extends AbstractDisguiseSessionRelatedEvent
{
    private static final HandlerList handlers = new HandlerList();

    public PlayerUnMorphEvent(@NotNull Player who, DisguiseState state)
    {
        super(who, state);
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
