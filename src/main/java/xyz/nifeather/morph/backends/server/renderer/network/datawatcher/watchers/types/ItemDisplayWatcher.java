package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Objects;

public class ItemDisplayWatcher extends DisplayEntityWatcher
{
    public ItemDisplayWatcher(Player bindTarget)
    {
        super(bindTarget, EntityType.ITEM_DISPLAY);
    }

    @Override
    public void onDisguiseApply()
    {
        super.onDisguiseApply();
        beginSelfUpdateIfAny();
    }

    private void beginSelfUpdateIfAny()
    {
        var player = getBindingPlayer();
        this.scheduleOn(player, () -> selfUpdate(player));
    }

    private volatile boolean hasCustomItemStack = false;
    private ItemStack lastDisplayingItemStack;

    private void selfUpdate(Player bindTarget)
    {
        if (this.disposed())
            return;

        if (!hasCustomItemStack)
        {
            var itemInMainHand = bindTarget.getEquipment().getItemInMainHand();
            if (!Objects.equals(itemInMainHand, lastDisplayingItemStack))
            {
                this.writePersistent(ValueIndex.ITEM_DISPLAY.ITEM_STACK, SpigotConversionUtil.fromBukkitItemStack(itemInMainHand));
                lastDisplayingItemStack = itemInMainHand;
            }
        }

        this.scheduleOn(bindTarget, () -> selfUpdate(bindTarget));
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();
        register(ValueIndex.ITEM_DISPLAY);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.ITEM_DISPLAY_DISPLAYING_ITEM ->
            {
                hasCustomItemStack = true;
                var bukkitItem = (ItemStack) value;
                this.writePersistent(ValueIndex.ITEM_DISPLAY.ITEM_STACK, SpigotConversionUtil.fromBukkitItemStack(bukkitItem));
            }

            case PropertyNames.ITEM_DISPLAY_MODEL_TRANSFORM ->
            {
                var transform = (ItemDisplay.ItemDisplayTransform) value;

                byte val = (byte) transform.ordinal();
                this.writePersistent(ValueIndex.ITEM_DISPLAY.ITEM_DISPLAY_MODEL, val);
            }
        }

        super.onPropertyWrite(property, value);
    }
}
