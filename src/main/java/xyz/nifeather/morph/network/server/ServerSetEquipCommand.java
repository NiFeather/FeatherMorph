package xyz.nifeather.morph.network.server;

import net.minecraft.SharedConstants;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.Map;

public class ServerSetEquipCommand extends S2CSetFakeEquipCommand<ItemStack>
{
    public ServerSetEquipCommand(ItemStack item, EquipmentSlot slot)
    {
        super(item, toProtocolEquipment(slot));
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        FeatherMorphMain.getInstance().getSLF4JLogger().info("FIXME: skipping " + getBaseName() + " to test equipment property");
        return Map.of();

        /*
        return Map.of(
                "slot", getSlot().toString(),
                "item", ItemUtils.itemToStr(getItemStack()),
                "data_version", "" + SharedConstants.getCurrentVersion().dataVersion().version()
        );*/
    }

    public static ProtocolEquipmentSlot toProtocolEquipment(EquipmentSlot slot)
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
