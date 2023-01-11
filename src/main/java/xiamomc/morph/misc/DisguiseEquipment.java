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

import java.util.List;
import java.util.Map;

public class DisguiseEquipment implements EntityEquipment
{
    private final Map<EquipmentSlot, ItemStack> itemStackMap = new Object2ObjectOpenHashMap<>();

    @Override
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item)
    {
        itemStackMap.put(slot, item);
    }

    @Override
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item, boolean silent)
    {
        this.setItem(slot, item);
    }

    @Override
    public @NotNull ItemStack getItem(@NotNull EquipmentSlot slot)
    {
        return DisguiseUtils.itemOrAir(itemStackMap.get(slot));
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
        return (ItemStack[]) List.of(
                getHelmet(),
                getChestplate(),
                getLeggings(),
                getBoots()
        ).toArray();
    }

    @Override
    public void setArmorContents(@NotNull ItemStack[] items)
    {
        this.setHelmet(items[3]);
        this.setChestplate(items[2]);
        this.setLeggings(items[1]);
        this.setBoots(items[0]);
    }

    @Override
    public void clear()
    {
        itemStackMap.clear();
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
