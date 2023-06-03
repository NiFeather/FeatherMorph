package xiamomc.morph.events.api.lifecycle;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityHandler;

public class AbilitiesFinishedInitializeEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public final AbilityHandler manager;

    public AbilitiesFinishedInitializeEvent(AbilityHandler instance)
    {
        this.manager = instance;
    }
}
