package xiamomc.morph.misc.animation.animations;

import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;
import xiamomc.morph.misc.skins.SingleSkin;

import java.util.List;

public class CatAnimationSet extends AnimationSet
{
    public final SingleAnimation LAY_START = new SingleAnimation(AnimationNames.LAY_START, 0, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 0, true);
    public final SingleAnimation SIT = new SingleAnimation(AnimationNames.SIT, 0, true);

    public CatAnimationSet()
    {
        register(AnimationNames.LAY, List.of(STANDUP, LAY_START));
        register(AnimationNames.SIT, List.of(STANDUP, SIT));
        register(AnimationNames.STANDUP, List.of(STANDUP));
    }
}
