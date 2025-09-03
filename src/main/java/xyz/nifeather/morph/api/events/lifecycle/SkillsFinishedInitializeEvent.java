package xyz.nifeather.morph.api.events.lifecycle;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.skills.SkillManager;

public class SkillsFinishedInitializeEvent extends Event
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

    public final SkillManager manager;

    public SkillsFinishedInitializeEvent(SkillManager instance)
    {
        this.manager = instance;
    }
}
