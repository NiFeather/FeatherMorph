package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.SingleAnimation;

import java.util.List;

public class PandaAnimationSet extends AnimationSet
{
    public final SingleAnimation SIT = new SingleAnimation(AnimationNames.SIT, 5, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 5, true);

    public PandaAnimationSet()
    {
        registerPersistent(AnimationNames.SIT, List.of(SIT));

        registerCommon(AnimationNames.STANDUP, List.of(STANDUP, RESET));
    }
}
