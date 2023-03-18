package xiamomc.morph.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ItemUtils
{
    private static final ItemStack air = new ItemStack(Material.AIR, 1);

    public static ItemStack itemOrAir(ItemStack stack)
    {
        return stack == null ? air.clone() : stack;
    }

    public static ItemStack asCopy(@NotNull ItemStack stack)
    {
        return stack.clone();
    }

    public static ItemStack[] asCopy(ItemStack... stacks)
    {
        var array = new ItemStack[stacks.length];

        for (int i = 0; i < stacks.length; i++)
        {
            var stack = stacks[i];
            array[i] = stack != null ? stack.clone() : null;
        }

        return array;
    }

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

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
            return gson.toJson(json.get());
        }

        return "{\"id\":\"minecraft:air\",\"Count\":1}";
    }

    /**
     * Check if the given {@link Material} is a continuous usable type (have consuming animation).
     * @param type {@link Material}
     * @return True if this type is continuous usable.
     */
    public static boolean isContinuousUsable(Material type)
    {
        return type == Material.BOW
                || type == Material.CROSSBOW
                || type == Material.TRIDENT
                || type == Material.SHIELD
                || type == Material.POTION
                || type == Material.MILK_BUCKET
                || type.isEdible();
    }
}
