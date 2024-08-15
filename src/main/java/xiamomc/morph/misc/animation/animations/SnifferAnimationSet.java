package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class SnifferAnimationSet extends AnimationSet
{
    private final SingleAnimation SNIFF = new SingleAnimation(AnimationNames.SNIFF, 20, true);

    public SnifferAnimationSet()
    {
        register(AnimationNames.SNIFF, List.of(SNIFF, RESET));
    }
}
