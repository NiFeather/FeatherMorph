package xyz.nifeather.morph.network.server;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.List;

public class ServerSetEquipCommand extends S2CSetFakeEquipCommand<ItemStack>
{
    public ServerSetEquipCommand(ItemStack item, EquipmentSlot slot)
    {
        super(item, toProtocolEquipment(slot));
    }

    @Override
    public List<String> serializeArgumentList()
    {
        var list = new ObjectArrayList<String>();

        list.add(getSlot().toString());
        list.add(ItemUtils.itemToStr(getItemStack()));

        return list;
    }

    private static ProtocolEquipmentSlot toProtocolEquipment(EquipmentSlot slot)
    {
        return switch (slot)
        {
            case HAND -> ProtocolEquipmentSlot.MAINHAND;
            case OFF_HAND -> ProtocolEquipmentSlot.OFF_HAND;
            case FEET -> ProtocolEquipmentSlot.BOOTS;
            case LEGS -> ProtocolEquipmentSlot.LEGGINGS;
            case CHEST -> ProtocolEquipmentSlot.CHESTPLATE;
            case HEAD -> ProtocolEquipmentSlot.HELMET;
            case BODY -> throw new IllegalArgumentException("BODY is not supported."); //生物BODY，和玩家无关？
            case SADDLE ->  throw new IllegalArgumentException("SADDLE is not supported.");
        };
    }
}
