package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class CatAnimationSet extends AnimationSet
{
    public final SingleAnimation LAY_START = new SingleAnimation(AnimationNames.LAY_START, 0, true);
    public final SingleAnimation LAY_STOP = new SingleAnimation(AnimationNames.LAY_STOP, 0, true);

    public CatAnimationSet()
    {
        register(AnimationNames.LAY, List.of(LAY_START));
        register(AnimationNames.STANDUP, List.of(LAY_STOP));
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of(AnimationNames.LAY, AnimationNames.STANDUP);
    }
}
