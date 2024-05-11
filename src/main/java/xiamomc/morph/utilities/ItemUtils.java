package xiamomc.morph.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.commands.data.EntityDataAccessor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;
import xiamomc.morph.MorphPlugin;

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

        if (stack.getType().isAir()) return "{\"id\":\"minecraft:air\",\"Count\":3}";

        //NOTE: 1.20.6需要访问世界注册表来获取JsonOps的序列化上下文来完整地反序列化ItemStack
        var registry = ((CraftWorld)Bukkit.getWorlds().stream().findFirst().get()).getHandle().registryAccess();

        //CODEC
        var nmsCodec = net.minecraft.world.item.ItemStack.CODEC;

        var json = nmsCodec.encodeStart(registry.createSerializationContext(JsonOps.INSTANCE), CraftItemStack.asNMSCopy(item))
                .result();

        if (json.isPresent())
        {
            return gson.toJson(json.get());
        }
        else
        {
            var logger = MorphPlugin.getInstance().getSLF4JLogger();

            logger.warn("Can't encode item '%s'! Providing without component data...".formatted(stack));

            return "{\"id\": \"%s\", \"Count\": 1}".formatted(stack.getType().getKey().asString());
        }
    }

    //TODO: CONVERT ITEM TO NBT
    public static String getItemCompound(ItemStack stack)
    {
        if (stack == null || stack.getAmount() == 0 || stack.getType().isAir())
            return "{}";

        var item = new NBTItem(stack);

        //TODO: TEST THIS
        return item.asNBTString();

        /*
        var item = ItemUtils.itemOrAir(stack);

        if (stack.getType().isAir()) return "{}";

        var nmsCopy = CraftItemStack.asNMSCopy(item);
        var components = nmsCopy.getComponents();

        throw new NotImplementedException("1.20.5 changed how item NBT works");

        //return NbtUtils.getCompoundString(nmsCopy.getTag());
        */
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
