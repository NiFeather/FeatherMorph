package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CRenderMapClearCommand extends NetheriteS2CCommand<String>
{
    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.CRClear;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onClientMapClearCommand(this);
    }
}
