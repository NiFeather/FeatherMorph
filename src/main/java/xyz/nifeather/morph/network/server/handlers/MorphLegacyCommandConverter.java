package xyz.nifeather.morph.network.server.handlers;

import xyz.nifeather.morph.network.commands.S2C.S2CCommandNames;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.fmccl.LegacyCommandConverter;
import xyz.nifeather.fmccl.network.commands.S2C.set.NetheriteS2CSetFakeEquipCommand;

public class MorphLegacyCommandConverter extends LegacyCommandConverter
{
    @Override
    protected void registerFromModernToNetherite()
    {
        super.registerFromModernToNetherite();

        this.registerModernToNetherite(S2CCommandNames.SetFakeEquip, ServerSetEquipCommand.class, cmd ->
                {
                    var modernSlot = cmd.getSlot();

                    NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot netheriteSlot = switch (modernSlot)
                    {
                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.MAINHAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.MAINHAND;
                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.OFF_HAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.OFF_HAND;

                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.HELMET -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.HELMET;
                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.CHESTPLATE -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.CHESTPLATE;
                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.LEGGINGS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.LEGGINGS;
                        case S2CSetFakeEquipCommand.ProtocolEquipmentSlot.BOOTS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.BOOTS;
                    };

                    return new LegacySetEquipCommand(cmd.getItemStack(), netheriteSlot);
                });
    }
}
