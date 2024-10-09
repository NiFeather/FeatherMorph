package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.SingleAnimation;

import java.util.List;

public class ShulkerAnimationSet extends AnimationSet
{
    public final SingleAnimation PEEK_START = new SingleAnimation(AnimationNames.PEEK_START, 40, true);
    public final SingleAnimation PEEK_STOP = new SingleAnimation(AnimationNames.PEEK_STOP, 20, true);

    public final SingleAnimation OPEN_START = new SingleAnimation(AnimationNames.OPEN_START, 100, true);
    public final SingleAnimation OPEN_STOP = new SingleAnimation(AnimationNames.OPEN_STOP, 20, true);

    public ShulkerAnimationSet()
    {
        registerCommon(AnimationNames.PEEK, List.of(PEEK_START, PEEK_STOP, RESET));
        registerCommon(AnimationNames.OPEN, List.of(OPEN_START, OPEN_STOP, RESET));
    }
}
