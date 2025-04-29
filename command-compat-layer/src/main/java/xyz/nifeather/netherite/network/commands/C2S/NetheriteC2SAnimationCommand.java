package xyz.nifeather.netherite.network.commands.C2S;

import xyz.nifeather.netherite.network.BasicClientHandler;

public class NetheriteC2SAnimationCommand extends NetheriteC2SCommand<String>
{
    public NetheriteC2SAnimationCommand(String animationId)
    {
        super(animationId);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteC2SCommandNames.Animation;
    }

    public static final String UNKNOWN_ANIMATION_ID = "morph:unknown";

    public String getAnimationId()
    {
        return getArgumentAt(0, UNKNOWN_ANIMATION_ID);
    }

    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        listener.onAnimationCommand(this);
    }
}
