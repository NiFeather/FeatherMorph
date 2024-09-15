package xiamomc.morph.providers.animation.bundled;

import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

import java.util.List;

public class FoxAnimationSet extends AnimationSet
{
    public final SingleAnimation SLEEP_START = new SingleAnimation(AnimationNames.SLEEP, 5, true);
    public final SingleAnimation SIT_START = new SingleAnimation(AnimationNames.SIT, 5, true);
    public final SingleAnimation STANDUP = new SingleAnimation(AnimationNames.STANDUP, 5, true);

    public FoxAnimationSet()
    {
        registerPersistent(AnimationNames.SLEEP, List.of(SLEEP_START));
        registerPersistent(AnimationNames.SIT, List.of(SIT_START));

        registerCommon(AnimationNames.STANDUP, List.of(STANDUP, RESET));
    }
}
