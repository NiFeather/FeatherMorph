package xiamomc.morph.backends.server.renderer.network;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.inventory.EntityEquipment;

public class ProtocolEquipment
{
    public static ObjectArrayList<Pair<EquipmentSlot, ItemStack>> toPairs(EntityEquipment equipment)
    {
        var list = new ObjectArrayList<Pair<EquipmentSlot, ItemStack>>();

        for (org.bukkit.inventory.EquipmentSlot bukkitSlot : org.bukkit.inventory.EquipmentSlot.values())
        {
            list.add(toEquipmentPair(equipment, bukkitSlot));
        }

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
        };
    }

    private static Pair<EquipmentSlot, ItemStack> toEquipmentPair(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        var bukkitItem = equipment.getItem(bukkitSlot);
        var nmsItem = ItemStack.fromBukkitCopy(bukkitItem);

        return Pair.of(toNMSSlot(bukkitSlot), nmsItem);
    }
}
