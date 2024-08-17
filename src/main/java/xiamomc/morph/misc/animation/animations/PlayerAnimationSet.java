package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class PlayerAnimationSet extends AnimationSet
{
    public final SingleAnimation LAY = new SingleAnimation(AnimationNames.LAY, 0, true);
    public final SingleAnimation PROSTRATE = new SingleAnimation(AnimationNames.CRAWL, 0, true);
    public final SingleAnimation STAND = new SingleAnimation(AnimationNames.STANDUP, 0, true);

    public PlayerAnimationSet()
    {
        register(AnimationNames.LAY, List.of(LAY));
        register(AnimationNames.CRAWL, List.of(PROSTRATE));
        register(AnimationNames.STANDUP, List.of(STAND, RESET));
    }
}
