package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class ParrotAnimationSet extends AnimationSet
{
    public final SingleAnimation DANCE_START = new SingleAnimation(AnimationNames.DANCE_START, 10, true);
    public final SingleAnimation DANCE_STOP = new SingleAnimation(AnimationNames.STOP, 10, true);

    public ParrotAnimationSet()
    {
        registerPersistent(AnimationNames.DANCE, List.of(DANCE_START));
        registerPersistent(AnimationNames.STOP, List.of(DANCE_STOP));
    }
}
