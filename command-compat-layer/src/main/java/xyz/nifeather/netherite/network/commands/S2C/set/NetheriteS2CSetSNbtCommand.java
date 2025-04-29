package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetSNbtCommand extends NetheriteS2CSetCommand<String>
{
    public NetheriteS2CSetSNbtCommand(String tag)
    {
        super(tag);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetSNbt;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetSNbtCommand(this);
    }
}
