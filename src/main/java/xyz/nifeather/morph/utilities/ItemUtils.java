package xyz.nifeather.morph.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;

public class ItemUtils
{
    public static final ItemStack air = new ItemStack(Material.AIR, 1);

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

    public static void extractItemCustomModel(ItemStack bukkitStack)
    {
        var nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
        var customModelData = nmsStack.get(DataComponents.CUSTOM_MODEL_DATA);

        if (customModelData == null)
            return;
    }

    @Nullable
    public static String getItemJsonName(ItemStack stack)
    {
        var itemMeta = stack.getItemMeta();
        Component nameToSerialize;

        if (itemMeta.hasCustomName())
            nameToSerialize = itemMeta.customName();
        else if (itemMeta.hasItemName())
            nameToSerialize = itemMeta.itemName();
        else
            return null;

        assert nameToSerialize != null;

        return JSONComponentSerializer.json().serialize(nameToSerialize);
    }

    public static String itemToStr(ItemStack stack)
    {
        var item = ItemUtils.itemOrAir(stack);

        if (stack.getType().isAir()) return "{\"id\":\"minecraft:air\",\"Count\":3}";

        //NOTE: 1.20.6需要访问世界注册表来获取JsonOps的序列化上下文来完整地反序列化ItemStack
        var registry = ((CraftWorld)Bukkit.getWorlds().stream().findFirst().orElseThrow()).getHandle().registryAccess();

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
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

            logger.warn("Can't encode item '%s'! Providing without component data...".formatted(stack));

            return "{\"id\": \"%s\", \"Count\": 1}".formatted(stack.getType().getKey().asString());
        }
    }

    //region Magic Bottle

    public static final String MAGIC_BOTTLE_ITEM_KEY = "feathermorph:is_magic_bottle";
    public static final String MAGIC_BOTTLE_STORE_ITEM_KEY = "feathermorph:magic_bottle_store";
    public static final String SKIP_MAGIC_BOTTLE_SETUP_KEY = "feathermorph:skip_magic_bottle_setup";
    public static final String ALWAYS_APPEND_REFERENCE_TOOLTIP_KEY = "feathermorph:always_append_reference_tooltip";

    public static boolean alwaysAppendReferenceTooltip(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData == null)
            return true;

        return customData.copyTag().getBooleanOr(ALWAYS_APPEND_REFERENCE_TOOLTIP_KEY, false);
    }

    public static boolean skipMagicBottleSetup(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData == null)
            return false;

        return customData.copyTag().getBooleanOr(SKIP_MAGIC_BOTTLE_SETUP_KEY, false);
    }

    public static ItemStack writeMagicItemData(ItemStack inputStack, String disguiseIdentifier)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(inputStack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData == null) customData = CustomData.EMPTY;

        customData = customData.update(tag -> tag.putString(MAGIC_BOTTLE_STORE_ITEM_KEY, disguiseIdentifier));
        nms.set(DataComponents.CUSTOM_DATA, customData);

        return nms.asBukkitMirror();
    }

    @Nullable
    public static String readMagicItemData(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);

        if (customData == null || !customData.contains(MAGIC_BOTTLE_STORE_ITEM_KEY))
            return null;

        return customData.copyTag().getString(MAGIC_BOTTLE_STORE_ITEM_KEY).orElse(null);
    }

    public static boolean isMagicItem(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);

        if (customData == null || !customData.contains(MAGIC_BOTTLE_ITEM_KEY)) return false;

        return customData.copyTag().getBoolean(MAGIC_BOTTLE_ITEM_KEY).orElseThrow() ;
    }

    //endregion Magic Bottle

    //region Disguise Tool

    public static final String SKILL_ACTIVATE_ITEM_KEY = "feathermorph:is_disguise_tool";

    public static ItemStack buildDisguiseToolFrom(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData == null) customData = CustomData.EMPTY;

        customData = customData.update(tag -> tag.putBoolean(SKILL_ACTIVATE_ITEM_KEY, true));
        nms.set(DataComponents.CUSTOM_DATA, customData);

        return nms.asBukkitMirror();
    }

    public static boolean isDisguiseTool(ItemStack stack)
    {
        var nms = net.minecraft.world.item.ItemStack.fromBukkitCopy(stack);
        var customData = nms.getComponents().get(DataComponents.CUSTOM_DATA);

        if (customData == null || !customData.contains(SKILL_ACTIVATE_ITEM_KEY)) return false;

        return customData.copyTag().getBoolean(SKILL_ACTIVATE_ITEM_KEY).orElseThrow() ;
    }

    //endregion Disguise Tool

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
                || type == Material.SPYGLASS
                || type.isEdible();
    }
}
