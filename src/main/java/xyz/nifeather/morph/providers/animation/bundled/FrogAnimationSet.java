package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.SingleAnimation;

import java.util.List;

public class FrogAnimationSet extends AnimationSet
{
    public final SingleAnimation EAT = new SingleAnimation(AnimationNames.EAT, 10, true);

    public FrogAnimationSet()
    {
        registerCommon(AnimationNames.EAT, List.of(EAT, RESET));
    }
}
