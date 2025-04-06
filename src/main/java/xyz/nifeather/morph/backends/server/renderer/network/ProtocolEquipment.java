package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.utilities.ItemUtils;

public class ProtocolEquipment
{
    public static ObjectArrayList<Equipment> toPEEquipmentList(EntityEquipment equipment)
    {
        var list = new ObjectArrayList<Equipment>();

        for (org.bukkit.inventory.EquipmentSlot bukkitSlot : org.bukkit.inventory.EquipmentSlot.values())
            list.add(toEquipment(equipment, bukkitSlot));

        return list;
    }

    private static EquipmentSlot toPESlot(org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        return switch (bukkitSlot)
        {
            case HAND -> EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;

            case HEAD -> EquipmentSlot.HELMET;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case FEET -> EquipmentSlot.BOOTS;

            case BODY -> EquipmentSlot.BODY;
            case SADDLE -> EquipmentSlot.SADDLE;
        };
    }

    private static Equipment toEquipment(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        try
        {
            if (equipment instanceof CraftInventoryPlayer && bukkitSlot == org.bukkit.inventory.EquipmentSlot.BODY)
                return new Equipment(toPESlot(bukkitSlot), ItemUtils.peAir);

            var bukkitItem = equipment.getItem(bukkitSlot);

            var peItem = SpigotConversionUtil.fromBukkitItemStack(bukkitItem);

            return new Equipment(toPESlot(bukkitSlot), peItem);
        }
        catch (Throwable t)
        {
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

            logger.warn("Can't generate equipment pair: " + t.getMessage());
        }

        return new Equipment(EquipmentSlot.BOOTS, ItemUtils.peAir);
    }
}
