package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetAnimationDisplayNameCommand extends NetheriteS2CSetCommand<String>
{
    public NetheriteS2CSetAnimationDisplayNameCommand(String id)
    {
        super(id);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetAnimationDisplayName;
    }

    public String getDisplayIdentifier()
    {
        return getArgumentAt(0, "nil");
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetAnimationDisplayCommand(this);
    }
}
