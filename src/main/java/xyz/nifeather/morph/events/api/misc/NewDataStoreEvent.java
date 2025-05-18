package xyz.nifeather.morph.events.api.misc;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.interfaces.IManagePlayerData;

public class NewDataStoreEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final MorphManager manager;

    public MorphManager morphManager()
    {
        return manager;
    }

    private final IManagePlayerData dataStore;

    public IManagePlayerData newDataStore()
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

    public NewDataStoreEvent(MorphManager manager, IManagePlayerData newDataStore)
    {
        this.manager = manager;
        this.dataStore = newDataStore;
    }
}
