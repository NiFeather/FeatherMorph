package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class PandaAnimationSet extends AnimationSet
{
    public final SingleAnimation SIT = new SingleAnimation(AnimationNames.SIT, 5, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 5, true);

    public PandaAnimationSet()
    {
        registerPersistent(AnimationNames.SIT, List.of(SIT));

        registerCommon(AnimationNames.STANDUP, List.of(STANDUP, RESET));
    }
}
