package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class ShulkerAnimationSet extends AnimationSet
{
    public final SingleAnimation PEEK_START = new SingleAnimation(AnimationNames.PEEK_START, 40, true);
    public final SingleAnimation PEEK_STOP = new SingleAnimation(AnimationNames.PEEK_STOP, 20, true);

    public final SingleAnimation OPEN_START = new SingleAnimation(AnimationNames.OPEN_START, 100, true);
    public final SingleAnimation OPEN_STOP = new SingleAnimation(AnimationNames.OPEN_STOP, 20, true);

    public ShulkerAnimationSet()
    {
        register(AnimationNames.PEEK, List.of(PEEK_START, PEEK_STOP));
        register(AnimationNames.OPEN, List.of(OPEN_START, OPEN_STOP));
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of(AnimationNames.PEEK, AnimationNames.OPEN);
    }
}
