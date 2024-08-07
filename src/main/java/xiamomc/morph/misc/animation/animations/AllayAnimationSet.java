package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class AllayAnimationSet extends AnimationSet
{
    public final SingleAnimation ROLL_START = new SingleAnimation(AnimationNames.CLIENT_DANCE_START, 100, true);
    public final SingleAnimation ROLL_STOP = new SingleAnimation(AnimationNames.CLIENT_DANCE_STOP, 0, true);

    public AllayAnimationSet()
    {
        register(AnimationNames.DANCE, List.of(ROLL_START, ROLL_STOP));
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of(AnimationNames.DANCE);
    }
}
