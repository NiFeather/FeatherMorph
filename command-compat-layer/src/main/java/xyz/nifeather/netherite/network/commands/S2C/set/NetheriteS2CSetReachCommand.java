package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetReachCommand extends NetheriteS2CSetCommand<Integer>
{
    public NetheriteS2CSetReachCommand(int reach)
    {
        super(reach);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetReach;
    }

    public int getReach()
    {
        return getArgumentAt(0, -1);
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetReach(this);
    }
}
