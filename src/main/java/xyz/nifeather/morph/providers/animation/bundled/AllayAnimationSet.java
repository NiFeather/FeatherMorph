package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.SingleAnimation;

import java.util.List;

public class AllayAnimationSet extends AnimationSet
{
    public final SingleAnimation ROLL_START = new SingleAnimation(AnimationNames.DANCE_START, 0, true);
    public final SingleAnimation ROLL_STOP = new SingleAnimation(AnimationNames.STOP, 0, true);

    public AllayAnimationSet()
    {
        registerPersistent(AnimationNames.DANCE, List.of(ROLL_START));

        registerCommon(AnimationNames.STOP, List.of(ROLL_STOP, RESET));
    }
}
