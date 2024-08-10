package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationHandler;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class PiglinAnimationSet extends AnimationSet
{
    public final SingleAnimation DANCING_START = new SingleAnimation(AnimationNames.DANCE_START, 0, true);
    public final SingleAnimation DANCING_STOP = new SingleAnimation(AnimationNames.STOP, 0, true);

    public PiglinAnimationSet()
    {
        register(AnimationNames.DANCE, List.of(DANCING_START));
        register(AnimationNames.STOP, List.of(DANCING_STOP));
    }
}
