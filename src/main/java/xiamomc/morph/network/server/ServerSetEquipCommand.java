package xiamomc.morph.network.server;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xiamomc.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xiamomc.morph.utilities.ItemUtils;

public class ServerSetEquipCommand extends S2CSetFakeEquipCommand<ItemStack>
{
    public ServerSetEquipCommand(ItemStack item, EquipmentSlot slot)
    {
        super(item, toProtocolEquipment(slot));
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + getSlot() + " " + ItemUtils.itemToStr(getItemStack());
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
        };
    }
}
