package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.DisguiseEquipment;

public class ProtocolEquipment
{
    public static ObjectArrayList<Equipment> toPEEquipmentList(DisguiseEquipment equipment)
    {
        var list = new ObjectArrayList<Equipment>();

        equipment.contents().forEach((slot, stack) ->
        {
            var packetEquipment = new Equipment(toPESlot(slot), SpigotConversionUtil.fromBukkitItemStack(stack));
            list.add(packetEquipment);
        });

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
}
