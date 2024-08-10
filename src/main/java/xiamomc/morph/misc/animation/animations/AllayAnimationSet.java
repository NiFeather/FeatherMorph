package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class AllayAnimationSet extends AnimationSet
{
    public final SingleAnimation ROLL_START = new SingleAnimation(AnimationNames.DANCE_START, 0, true);
    public final SingleAnimation ROLL_STOP = new SingleAnimation(AnimationNames.STOP, 0, true);

    public AllayAnimationSet()
    {
        register(AnimationNames.DANCE, List.of(ROLL_START));
        register(AnimationNames.STOP, List.of(ROLL_STOP));
    }
}
