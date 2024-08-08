package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class PufferfishAnimationSet extends AnimationSet
{
    public final SingleAnimation INFLATE = new SingleAnimation(AnimationNames.INFLATE, 0, true);
    public final SingleAnimation DEFLATE = new SingleAnimation(AnimationNames.DEFLATE, 0, true);

    public PufferfishAnimationSet()
    {
        register(AnimationNames.INFLATE, List.of(INFLATE));
        register(AnimationNames.DEFLATE, List.of(DEFLATE));
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of(AnimationNames.INFLATE, AnimationNames.DEFLATE);
    }
}
