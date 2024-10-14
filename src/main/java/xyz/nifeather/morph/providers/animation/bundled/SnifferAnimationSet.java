package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.SingleAnimation;

import java.util.List;

public class SnifferAnimationSet extends AnimationSet
{
    private final SingleAnimation SNIFF = new SingleAnimation(AnimationNames.SNIFF, 20, true);

    public SnifferAnimationSet()
    {
        registerCommon(AnimationNames.SNIFF, List.of(SNIFF, RESET));
    }
}
