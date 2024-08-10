package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class ParrotAnimationSet extends AnimationSet
{
    public final SingleAnimation DANCE_START = new SingleAnimation(AnimationNames.DANCE_START, 10, true);
    public final SingleAnimation DANCE_STOP = new SingleAnimation(AnimationNames.STOP, 10, true);

    public ParrotAnimationSet()
    {
        register(AnimationNames.DANCE, List.of(DANCE_START));
        register(AnimationNames.STOP, List.of(DANCE_STOP));
    }
}
