package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.ItemUtils;

import java.util.Arrays;
import java.util.Map;

public class DisguiseEquipment implements EntityEquipment
{
    private final ItemStack[] itemStacks = new ItemStack[EquipmentSlot.values().length];

    private final Map<EquipmentSlot, ItemStack> dirtyStacks = new Object2ObjectOpenHashMap<>();

    public Map<EquipmentSlot, ItemStack> getDirty()
    {
        var map = new Object2ObjectOpenHashMap<>(dirtyStacks);
        dirtyStacks.clear();

        return map;
    }

    @Override
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item)
    {
        itemStacks[slot.ordinal()] = item;
    }

    @Override
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item, boolean silent)
    {
        this.setItem(slot, item);
    }

    public void setHandItems(@Nullable ItemStack... stacks)
    {
        if (stacks == null)
        {
            setItemInHand(null);
            setItemInOffHand(null);
            return;
        }

        setItemInMainHand(stacks.length >= 1 ? stacks[0] : null);
        setItemInOffHand(stacks.length >= 2 ? stacks[1] : null);
    }

    public boolean allowNull = false;

    /**
     * Nullable if {@link DisguiseEquipment#allowNull} is true.
     * @param slot the slot to get the ItemStack
     * @return An {@link ItemStack}
     */
    @Override
    public @NotNull ItemStack getItem(@NotNull EquipmentSlot slot)
    {
        return allowNull ? itemStacks[slot.ordinal()] : DisguiseUtils.itemOrAir(itemStacks[slot.ordinal()]);
    }

    @Override
    public @NotNull ItemStack getItemInMainHand()
    {
        return getItem(EquipmentSlot.HAND);
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack item)
    {
        setItem(EquipmentSlot.HAND, item);
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack item, boolean silent)
    {
        this.setItemInHand(item);
    }

    @Override
    public @NotNull ItemStack getItemInOffHand()
    {
        return getItem(EquipmentSlot.OFF_HAND);
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack item)
    {
        setItem(EquipmentSlot.OFF_HAND, item);
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack item, boolean silent)
    {
        this.setItemInOffHand(item);
    }

    @Override
    public @NotNull ItemStack getItemInHand()
    {
        return this.getItemInMainHand();
    }

    @Override
    public void setItemInHand(@Nullable ItemStack stack)
    {
        this.setItemInMainHand(stack);
    }

    @Override
    public ItemStack getHelmet()
    {
        return getItem(EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmet(@Nullable ItemStack helmet)
    {
        setItem(EquipmentSlot.HEAD, helmet);
    }

    @Override
    public void setHelmet(@Nullable ItemStack helmet, boolean silent)
    {
        this.setHelmet(helmet);
    }

    @Override
    public ItemStack getChestplate()
    {
        return getItem(EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplate(@Nullable ItemStack chestplate)
    {
        this.setItem(EquipmentSlot.CHEST, chestplate);
    }

    @Override
    public void setChestplate(@Nullable ItemStack chestplate, boolean silent)
    {
        this.setChestplate(chestplate);
    }

    @Override
    public ItemStack getLeggings()
    {
        return getItem(EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggings(@Nullable ItemStack leggings)
    {
        this.setItem(EquipmentSlot.LEGS, leggings);
    }

    @Override
    public void setLeggings(@Nullable ItemStack leggings, boolean silent)
    {
        this.setLeggings(leggings);
    }

    @Override
    public ItemStack getBoots()
    {
        return getItem(EquipmentSlot.FEET);
    }

    @Override
    public void setBoots(@Nullable ItemStack boots)
    {
        setItem(EquipmentSlot.FEET, boots);
    }

    @Override
    public void setBoots(@Nullable ItemStack boots, boolean silent)
    {
        this.setBoots(boots);
    }

    @Override
    public ItemStack @NotNull [] getArmorContents()
    {
        return new ItemStack[]
                {
                        getBoots(),
                        getLeggings(),
                        getChestplate(),
                        getHelmet(),
                };
    }

    @Override
    public void setArmorContents(@NotNull ItemStack[] items)
    {
        var air = ItemUtils.itemOrAir(null);

        this.setBoots(items.length >= 1 ? items[0] : air);
        this.setLeggings(items.length >= 2 ? items[1] : air);
        this.setChestplate(items.length >= 3 ? items[2] : air);
        this.setHelmet(items.length >= 4 ? items[3] : air);
    }

    public ItemStack @NotNull [] getHandItems()
    {
        return new ItemStack[]
                {
                        getItemInMainHand(),
                        getItemInOffHand()
                };
    }

    @Override
    public void clear()
    {
        Arrays.fill(itemStacks, null);
    }

    @Override
    public float getItemInHandDropChance()
    {
        return 0;
    }

    @Override
    public void setItemInHandDropChance(float chance)
    {

    }

    @Override
    public float getItemInMainHandDropChance()
    {
        return 0;
    }

    @Override
    public void setItemInMainHandDropChance(float chance)
    {

    }

    @Override
    public float getItemInOffHandDropChance()
    {
        return 0;
    }

    @Override
    public void setItemInOffHandDropChance(float chance)
    {

    }

    @Override
    public float getHelmetDropChance()
    {
        return 0;
    }

    @Override
    public void setHelmetDropChance(float chance)
    {

    }

    @Override
    public float getChestplateDropChance()
    {
        return 0;
    }

    @Override
    public void setChestplateDropChance(float chance)
    {

    }

    @Override
    public float getLeggingsDropChance()
    {
        return 0;
    }

    @Override
    public void setLeggingsDropChance(float chance)
    {

    }

    @Override
    public float getBootsDropChance()
    {
        return 0;
    }

    @Override
    public void setBootsDropChance(float chance)
    {

    }

    @Override
    public @NotNull Entity getHolder()
    {
        throw new NotImplementedException("no");
    }

    @Override
    public float getDropChance(@NotNull EquipmentSlot slot)
    {
        return 0;
    }

    @Override
    public void setDropChance(@NotNull EquipmentSlot slot, float chance)
    {

    }
}
