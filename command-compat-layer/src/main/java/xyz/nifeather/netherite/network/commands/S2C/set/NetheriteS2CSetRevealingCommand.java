package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetRevealingCommand extends NetheriteS2CSetCommand<Float>
{
    public NetheriteS2CSetRevealingCommand(float val)
    {
        super(val);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetRevealing;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetRevealing(this);
    }

    public float getValue()
    {
        return getArgumentAt(0, 0f);
    }
}
