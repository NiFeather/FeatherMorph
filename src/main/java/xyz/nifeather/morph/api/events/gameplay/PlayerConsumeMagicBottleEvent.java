package xyz.nifeather.morph.api.events.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerConsumeMagicBottleEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() { return handlers; }

    private boolean cancelled = false;
    private final ItemStack consumedItem;

    public PlayerConsumeMagicBottleEvent(Player player, ItemStack consumedItem)
    {
        super(player);
        this.consumedItem = consumedItem;
    }

    /**
     * @apiNote The consumed item may not be a potion, but any item that has both the "feathermorph:is_magic_bottle" and "feathermorph:magic_bottle_store" data
     * @return The item consumed from {@link PlayerItemConsumeEvent}
     */
    public ItemStack getConsumedItem()
    {
        return consumedItem;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = true;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }
}
