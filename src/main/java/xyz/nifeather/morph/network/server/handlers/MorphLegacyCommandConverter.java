package xyz.nifeather.morph.network.server.handlers;

import xyz.nifeather.fmccl.converter.S2CCommandConverter;
import xyz.nifeather.fmccl.network.commands.S2C.set.NetheriteS2CSetFakeEquipCommand;
import xyz.nifeather.morph.network.commands.S2C.S2CCommandNames;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.morph.network.utils.ProtocolEquipmentSlot;

public class MorphLegacyCommandConverter extends S2CCommandConverter
{
    @Override
    protected void registerFakeEquipCommandConversions()
    {
        this.registerModernToNetherite(S2CCommandNames.SetFakeEquip, ServerSetEquipCommand.class, cmd ->
        {
            var modernSlot = cmd.getSlot();

            NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot netheriteSlot = switch (modernSlot)
            {
                case ProtocolEquipmentSlot.MAINHAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.MAINHAND;
                case ProtocolEquipmentSlot.OFF_HAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.OFF_HAND;

                case ProtocolEquipmentSlot.HELMET -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.HELMET;
                case ProtocolEquipmentSlot.CHESTPLATE -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.CHESTPLATE;
                case ProtocolEquipmentSlot.LEGGINGS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.LEGGINGS;
                case ProtocolEquipmentSlot.BOOTS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.BOOTS;
            };

            return new LegacySetEquipCommand(cmd.getItemStack(), netheriteSlot);
        });
    }
}
