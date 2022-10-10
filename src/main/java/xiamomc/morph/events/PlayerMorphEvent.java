package xiamomc.morph.events;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.DisguiseState;

public class PlayerMorphEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();

    private final DisguiseState state;

    public PlayerMorphEvent(@NotNull Player who, @NotNull DisguiseState state)
    {
        super(who);

        this.state = state;
    }

    public @NotNull DisguiseState getState()
    {
        return state;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
