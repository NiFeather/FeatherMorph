package xyz.nifeather.netherite.network.commands.S2C;

import xyz.nifeather.netherite.network.BasicServerHandler;

public class NetheriteS2CAnimationCommand extends NetheriteS2CCommand<String>
{
    public NetheriteS2CAnimationCommand(String animation)
    {
        super(animation);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.Animation;
    }

    public String getAnimId()
    {
        return getArgumentAt(0, "nil");
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onAnimationCommand(this);
    }
}
