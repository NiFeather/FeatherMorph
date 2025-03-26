package xyz.nifeather.morph.events.api.gameplay;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player changes their town's FeatherMorph flags.
 */
public class MorphTownBooleanFlagChangedEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }

    private final BooleanDataField dataField;
    private final boolean newValue;
    private final Town town;

    public MorphTownBooleanFlagChangedEvent(@NotNull Player executor, @NotNull Town town, @NotNull BooleanDataField dataField, boolean newValue)
    {
        super(executor);

        this.dataField = dataField;
        this.newValue = newValue;
        this.town = town;
    }

    public BooleanDataField getDataField()
    {
        return dataField;
    }

    public boolean getNewValue()
    {
        return newValue;
    }

    public Town getTown()
    {
        return town;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }
}
