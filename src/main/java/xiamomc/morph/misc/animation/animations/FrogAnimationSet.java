package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class FrogAnimationSet extends AnimationSet
{
    public final SingleAnimation EAT = new SingleAnimation(AnimationNames.EAT, 10, true);

    public FrogAnimationSet()
    {
        register(AnimationNames.EAT, List.of(EAT, RESET));
    }
}
