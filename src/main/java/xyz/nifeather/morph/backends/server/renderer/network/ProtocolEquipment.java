package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;

public class ProtocolEquipment
{
    public static ObjectArrayList<Equipment> toPEEquipmentList(EntityEquipment equipment)
    {
        var list = new ObjectArrayList<Equipment>();

        for (org.bukkit.inventory.EquipmentSlot bukkitSlot : org.bukkit.inventory.EquipmentSlot.values())
        {
            var packetEquipment = toEquipment(equipment, bukkitSlot);

            if (packetEquipment != null)
                list.add(packetEquipment);
        }

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

    public static final com.github.retrooper.packetevents.protocol.item.ItemStack peAir = new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder()
            .type(ItemTypes.AIR).build();

    @Nullable
    private static Equipment toEquipment(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot bukkitSlot)
    {
        if (bukkitSlot == org.bukkit.inventory.EquipmentSlot.SADDLE || bukkitSlot == org.bukkit.inventory.EquipmentSlot.BODY)
            return null;

        try
        {
            //if (equipment instanceof CraftInventoryPlayer && bukkitSlot == org.bukkit.inventory.EquipmentSlot.BODY)
            //    return new Equipment(toPESlot(bukkitSlot), ItemUtils.peAir);

            var bukkitItem = equipment.getItem(bukkitSlot);

            var peItem = SpigotConversionUtil.fromBukkitItemStack(bukkitItem);

            return new Equipment(toPESlot(bukkitSlot), peItem);
        }
        catch (Throwable t)
        {
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

            logger.warn("Can't generate equipment pair: " + t.getMessage());
            t.printStackTrace();
        }

        return new Equipment(EquipmentSlot.BOOTS, peAir);
    }
}
