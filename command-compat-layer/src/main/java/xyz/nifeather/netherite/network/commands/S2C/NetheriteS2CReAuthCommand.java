package xyz.nifeather.netherite.network.commands.S2C;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

public class NetheriteS2CReAuthCommand extends NetheriteS2CCommand<String>
{
    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.ReAuth;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onReAuthCommand(this);
    }
}
