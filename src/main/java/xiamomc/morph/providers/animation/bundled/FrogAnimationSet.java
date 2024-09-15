package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class FrogAnimationSet extends AnimationSet
{
    public final SingleAnimation EAT = new SingleAnimation(AnimationNames.EAT, 10, true);

    public FrogAnimationSet()
    {
        registerCommon(AnimationNames.EAT, List.of(EAT, RESET));
    }
}
