package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import net.minecraft.world.entity.Display;

public class ItemDisplayValues extends DisplayEntityValues
{
    //Item Display
    public final SingleValue<ItemStack> ITEM_STACK = createSingle("item_display_item_stack", ItemStack.builder().type(ItemTypes.STONE).build(), EntityDataTypes.ITEMSTACK);
    public final SingleValue<Byte> ITEM_DISPLAY_MODEL = createSingle("item_display_item_display_model", (byte)0, EntityDataTypes.BYTE);

    public ItemDisplayValues()
    {
        super();

        registerSingle(
                ITEM_STACK,
                ITEM_DISPLAY_MODEL
        );
    }
}
