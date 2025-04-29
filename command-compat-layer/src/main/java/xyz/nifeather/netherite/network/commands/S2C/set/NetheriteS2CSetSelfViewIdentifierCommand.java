package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetSelfViewIdentifierCommand extends NetheriteS2CSetCommand<String>
{
    public NetheriteS2CSetSelfViewIdentifierCommand(String identifier)
    {
        super(identifier);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetSelfViewIdentifier;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetSelfViewIdentifierCommand(this);
    }
}
