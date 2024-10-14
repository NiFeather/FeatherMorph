package xyz.nifeather.morph.transforms.easings;

import xyz.nifeather.morph.transforms.easings.impl.EasingImpl;

/**
 * <a href="https://easings.net">easings.net</a>
 */
public enum Easing
{
    Plain,

    InSine,
    InCubic,
    InQuint,
    InCirc,
    InElastic,
    InQuad,
    InQuart,
    InExpo,
    InBack,
    InBounce,

    OutSine,
    OutCubic,
    OutQuint,
    OutCirc,
    OutElastic,
    OutQuad,
    OutQuart,
    OutExpo,
    OutBack,
    OutBounce,

    InOutSine,
    InOutCubic,
    InOutQuint,
    InOutCirc,
    InOutElastic,
    InOutQuad,
    InOutQuart,
    InOutExpo,
    InOutBack,
    InOutBounce;

    public final IEasing getImpl()
    {
        return new EasingImpl(this);
    }
}
