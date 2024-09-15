package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class CatAnimationSet extends AnimationSet
{
    public final SingleAnimation LAY = new SingleAnimation(AnimationNames.LAY_START, 0, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 0, true);
    public final SingleAnimation SIT = new SingleAnimation(AnimationNames.SIT, 0, true);

    public CatAnimationSet()
    {
        registerPersistent(AnimationNames.LAY, List.of(RESET, LAY));
        registerPersistent(AnimationNames.SIT, List.of(RESET, SIT));

        registerCommon(AnimationNames.STANDUP, List.of(STANDUP, RESET));
    }
}
