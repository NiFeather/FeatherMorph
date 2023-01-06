package xiamomc.morph.misc;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemUtils
{
    public static ItemStack itemOrAir(ItemStack stack)
    {
        return stack == null ? air : stack;
    }

    public static String itemToStr(ItemStack stack)
    {
        var item = ItemUtils.itemOrAir(stack);

        if (stack.getType().isAir()) return "{\"id\":\"minecraft:air\",\"Count\":1}";

        //CODEC
        var nmsCodec = net.minecraft.world.item.ItemStack.CODEC;
        var json = nmsCodec.encode(CraftItemStack.asNMSCopy(item), JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
                .result();

        if (json.isPresent())
        {
            var gson = new Gson();
            return gson.toJson(json.get());
        }

        return "{\"id\":\"minecraft:air\",\"Count\":1}";
    }

    private static final ItemStack air = new ItemStack(Material.AIR);
}
