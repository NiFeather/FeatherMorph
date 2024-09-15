package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class PiglinAnimationSet extends AnimationSet
{
    public final SingleAnimation DANCING_START = new SingleAnimation(AnimationNames.DANCE_START, 0, true);
    public final SingleAnimation DANCING_STOP = new SingleAnimation(AnimationNames.STOP, 0, true);

    public PiglinAnimationSet()
    {
        registerPersistent(AnimationNames.DANCE, List.of(DANCING_START));

        registerCommon(AnimationNames.STOP, List.of(DANCING_STOP, RESET));
    }
}
