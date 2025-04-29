package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetModifyBoundingBoxCommand extends NetheriteS2CSetCommand<Boolean>
{
    public NetheriteS2CSetModifyBoundingBoxCommand(boolean val)
    {
        super(val);
    }

    public boolean getModifyBoundingBox()
    {
        return getArgumentAt(0, false);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetModifyBoundingBox;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetModifyBoundingBox(this);
    }
}
