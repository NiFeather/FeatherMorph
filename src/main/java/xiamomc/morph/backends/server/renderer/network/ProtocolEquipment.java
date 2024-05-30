package xiamomc.morph.backends.server.renderer.network;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.utilities.ItemUtils;

public class ProtocolEquipment
{
    public static ObjectArrayList<Pair<EquipmentSlot, ItemStack>> toPairs(EntityEquipment equipment)
    {
        var list = new ObjectArrayList<Pair<EquipmentSlot, ItemStack>>();

        for (org.bukkit.inventory.EquipmentSlot bukkitSlot : org.bukkit.inventory.EquipmentSlot.values())
            list.add(toEquipmentPair(equipment, bukkitSlot));

        return list;
    }

    private static EquipmentSlot toNMSSlot(org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        return switch (bukkitSlot)
        {
            case HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;

            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;

            case BODY -> throw new IllegalArgumentException("BODY is not supported.");
        };
    }

    private static Pair<EquipmentSlot, ItemStack> toEquipmentPair(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        try
        {
            if (equipment instanceof CraftInventoryPlayer && bukkitSlot == org.bukkit.inventory.EquipmentSlot.BODY)
                return Pair.of(toNMSSlot(bukkitSlot), ItemUtils.nmsAir);

            var bukkitItem = equipment.getItem(bukkitSlot);
            var nmsItem = ItemStack.fromBukkitCopy(bukkitItem);

            return Pair.of(toNMSSlot(bukkitSlot), nmsItem);
        }
        catch (Throwable t)
        {
            var logger = MorphPlugin.getInstance().getSLF4JLogger();

            logger.warn("Can't generate equipment pair: " + t.getMessage());
        }

        return Pair.of(EquipmentSlot.FEET, ItemStack.fromBukkitCopy(new org.bukkit.inventory.ItemStack(Material.AIR)));
    }
}
