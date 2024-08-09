package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class PandaAnimationSet extends AnimationSet
{
    public final SingleAnimation SIT = new SingleAnimation(AnimationNames.SIT, 5, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 5, true);

    public PandaAnimationSet()
    {
        register(AnimationNames.SIT, List.of(SIT));
        register(AnimationNames.STANDUP, List.of(STANDUP));
    }
}
