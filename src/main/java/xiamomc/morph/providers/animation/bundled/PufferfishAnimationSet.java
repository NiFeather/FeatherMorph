package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class PufferfishAnimationSet extends AnimationSet
{
    public final SingleAnimation INFLATE = new SingleAnimation(AnimationNames.INFLATE, 0, true);
    public final SingleAnimation DEFLATE = new SingleAnimation(AnimationNames.DEFLATE, 0, true);

    public PufferfishAnimationSet()
    {
        registerPersistent(AnimationNames.INFLATE, List.of(INFLATE));

        registerCommon(AnimationNames.DEFLATE, List.of(DEFLATE, RESET));
    }
}
