package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetDisplayingFakeEquipCommand extends NetheriteS2CSetCommand<Boolean>
{
    public NetheriteS2CSetDisplayingFakeEquipCommand(boolean val)
    {
        super(val);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetDisplayingFakeEquip;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetDisplayingFakeEquipCommand(this);
    }
}
