package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;

public class PlayerSwitchMorphEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    private final DisguiseState previousState;
    public DisguiseState previousState()
    {
        return previousState;
    }

    private final DisguiseState nextState;
    public DisguiseState nextState()
    {
        return nextState;
    }

    public PlayerSwitchMorphEvent(Player player, DisguiseState previousState, DisguiseState newState)
    {
        super(player);
        this.previousState = previousState;
        this.nextState = newState;
    }
}
