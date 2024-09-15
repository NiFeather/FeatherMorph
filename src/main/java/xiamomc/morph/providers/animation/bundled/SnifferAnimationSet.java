package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class SnifferAnimationSet extends AnimationSet
{
    private final SingleAnimation SNIFF = new SingleAnimation(AnimationNames.SNIFF, 20, true);

    public SnifferAnimationSet()
    {
        registerCommon(AnimationNames.SNIFF, List.of(SNIFF, RESET));
    }
}
