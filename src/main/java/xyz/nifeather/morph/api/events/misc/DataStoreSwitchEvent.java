package xyz.nifeather.morph.api.events.misc;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.storage.IPlayerDataBackend;

public class DataStoreSwitchEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final MorphManager manager;

    public MorphManager morphManager()
    {
        return manager;
    }

    private final IPlayerDataBackend dataStore;

    public IPlayerDataBackend newDataStore()
    {
        return dataStore;
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

    public DataStoreSwitchEvent(MorphManager manager, IPlayerDataBackend newDataStore)
    {
        this.manager = manager;
        this.dataStore = newDataStore;
    }
}
