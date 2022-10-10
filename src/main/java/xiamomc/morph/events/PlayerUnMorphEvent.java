package xiamomc.morph.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerUnMorphEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();

    public PlayerUnMorphEvent(@NotNull Player who)
    {
        super(who);
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
