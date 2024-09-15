package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

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
