package xyz.nifeather.morph.network.server.handlers;

import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.utilities.ItemUtils;
import xyz.nifeather.netherite.network.commands.S2C.set.NetheriteS2CSetFakeEquipCommand;

public class LegacySetEquipCommand extends NetheriteS2CSetFakeEquipCommand<ItemStack>
{
    public LegacySetEquipCommand(ItemStack item, ProtocolEquipmentSlot slot)
    {
        super(item, slot);
    }

    @Override
    public String serializeArguments()
    {
        return getSlot().toString() + " " + ItemUtils.itemToStr(getItemStack());
    }
}
