package xyz.nifeather.morph.misc;

import com.google.common.collect.ImmutableMap;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DisguiseEquipment implements ISupportDiffs<DisguiseEquipment>
{
    private final Map<EquipmentSlot, ItemStack> itemStackMap = new ConcurrentHashMap<>();

    public DisguiseEquipment(Map<EquipmentSlot, ItemStack> items)
    {
        itemStackMap.putAll(items);
    }

    @Unmodifiable
    public Map<EquipmentSlot, ItemStack> contents()
    {
        return ImmutableMap.copyOf(itemStackMap);
    }

    /**
     * @param slot the slot to get the ItemStack
     * @return An {@link ItemStack}
     */
    public @NotNull ItemStack getItem(@NotNull EquipmentSlot slot)
    {
        return itemStackMap.getOrDefault(slot, ItemUtils.air.clone());
    }

    public @Nullable ItemStack getItemOrNull(@Nullable EquipmentSlot slot)
    {
        return itemStackMap.getOrDefault(slot, null);
    }

    public @NotNull ItemStack getItemInMainHand()
    {
        return getItem(EquipmentSlot.HAND);
    }

    public @NotNull ItemStack getItemInOffHand()
    {
        return getItem(EquipmentSlot.OFF_HAND);
    }

    public ItemStack getHelmet()
    {
        return getItem(EquipmentSlot.HEAD);
    }

    public ItemStack getChestplate()
    {
        return getItem(EquipmentSlot.CHEST);
    }

    public ItemStack getLeggings()
    {
        return getItem(EquipmentSlot.LEGS);
    }

    public ItemStack getBoots()
    {
        return getItem(EquipmentSlot.FEET);
    }

    public boolean filterAll(Function<ItemStack, Boolean> filter)
    {
        boolean pass = true;
        for (Map.Entry<EquipmentSlot, ItemStack> entry : itemStackMap.entrySet())
            pass = filter.apply(entry.getValue()) && pass;

        return pass;
    }

    public DisguiseEquipmentBuilder cloneForEdit()
    {
        return builder(this);
    }

    @Override
    public String toString()
    {
        return "DisguiseEquipment[%s]".formatted(itemStackMap.toString());
    }

    /**
     * Returns a builder that's pre-filled all slots with air
     */
    public static DisguiseEquipmentBuilder builder()
    {
        return new DisguiseEquipmentBuilder();
    }

    /**
     * Returns a builder that's pre-filled with the given equipment.<br>
     * If the equipment has a slot that's NULL, it will be filled with air
     */
    public static DisguiseEquipmentBuilder builder(EntityEquipment other)
    {
        return new DisguiseEquipmentBuilder(other);
    }

    /**
     * Returns a builder that's pre-filled with the given equipment.<br>
     * If the equipment has a slot that's NULL, it will be filled with air
     */
    public static DisguiseEquipmentBuilder builder(DisguiseEquipment other)
    {
        return new DisguiseEquipmentBuilder(other.itemStackMap);
    }

    /**
     * Returns a builder that's pre-filled with the given map.<br>
     * This builder will <b>NOT</b> pre-fill any slot with air
     */
    public static DisguiseEquipmentBuilder builder(Map<EquipmentSlot, ItemStack> map)
    {
        return new DisguiseEquipmentBuilder(map);
    }

    /**
     * See {@link DisguiseEquipmentBuilder#builder(EntityEquipment)}
     */
    public static DisguiseEquipment copy(EntityEquipment other)
    {
        return builder(other).build();
    }

    /**
     * A pure empty DisguiseEquipment
     */
    public static DisguiseEquipment empty()
    {
        return builder().build();
    }

    @Override
    public DisguiseEquipment diff(DisguiseEquipment other)
    {
        Map<EquipmentSlot, ItemStack> changes = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            var ours = getItemOrNull(slot);
            var theirs = other.getItemOrNull(slot);

            if (!Objects.equals(ours, theirs))
                changes.put(slot, ItemUtils.itemOrAir(theirs).clone());
        }

        return new DisguiseEquipment(changes);
    }

    public static class DisguiseEquipmentBuilder
    {
        private final Map<EquipmentSlot, ItemStack> itemMap = new ConcurrentHashMap<>();

        private static Map<EquipmentSlot, ItemStack> airMap()
        {
            var itemMap = new ConcurrentHashMap<EquipmentSlot, ItemStack>();
            for (EquipmentSlot value : EquipmentSlot.values())
                itemMap.put(value, ItemUtils.air.clone());

            return itemMap;
        }

        public DisguiseEquipmentBuilder()
        {
            this(airMap());
        }

        public DisguiseEquipmentBuilder(@NotNull EntityEquipment entityEquipment)
        {
            this(Map.of(
                    EquipmentSlot.HEAD, ItemUtils.itemOrAir(entityEquipment.getHelmet()).clone(),
                    EquipmentSlot.CHEST, ItemUtils.itemOrAir(entityEquipment.getChestplate()).clone(),
                    EquipmentSlot.LEGS, ItemUtils.itemOrAir(entityEquipment.getLeggings()).clone(),
                    EquipmentSlot.FEET, ItemUtils.itemOrAir(entityEquipment.getBoots()).clone(),

                    EquipmentSlot.HAND, ItemUtils.itemOrAir(entityEquipment.getItemInMainHand()).clone(),
                    EquipmentSlot.OFF_HAND, ItemUtils.itemOrAir(entityEquipment.getItemInOffHand()).clone()
            ));
        }

        public DisguiseEquipmentBuilder(Map<EquipmentSlot, ItemStack> existing)
        {
            itemMap.putAll(existing);
        }

        public DisguiseEquipmentBuilder helmet(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.HEAD, stack);
        }

        public DisguiseEquipmentBuilder chestplate(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.CHEST, stack);
        }

        public DisguiseEquipmentBuilder leggings(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.LEGS, stack);
        }

        public DisguiseEquipmentBuilder boots(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.FEET, stack);
        }

        public DisguiseEquipmentBuilder mainHand(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.HAND, stack);
        }

        public DisguiseEquipmentBuilder offHand(@NotNull ItemStack stack)
        {
            return forSlot(EquipmentSlot.OFF_HAND, stack);
        }

        public DisguiseEquipmentBuilder forSlot(EquipmentSlot slot, @NotNull ItemStack stack)
        {
            Objects.requireNonNull(stack, "Null item is not accepted");
            itemMap.put(slot, stack);
            return this;
        }

        public DisguiseEquipmentBuilder mergeIfNotNull(@Nullable DisguiseEquipment other)
        {
            if (other != null) merge(other);

            return this;
        }

        public DisguiseEquipmentBuilder merge(DisguiseEquipment other)
        {
            other.itemStackMap.forEach(this::forSlot);
            return this;
        }

        public DisguiseEquipment build()
        {
            return new DisguiseEquipment(itemMap);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DisguiseEquipment other)) return false;
        return other.itemStackMap.equals(this.itemStackMap);
    }
}
