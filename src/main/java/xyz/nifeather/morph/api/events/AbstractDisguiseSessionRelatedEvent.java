package xyz.nifeather.morph.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import xyz.nifeather.morph.misc.DisguiseState;

public abstract class AbstractDisguiseSessionRelatedEvent extends PlayerEvent
{
    private final DisguiseState bindingState;

    public DisguiseState bindingState()
    {
        return bindingState;
    }

    public AbstractDisguiseSessionRelatedEvent(Player player, DisguiseState bindingState)
    {
        super(player);

        this.bindingState = bindingState;
    }
}
