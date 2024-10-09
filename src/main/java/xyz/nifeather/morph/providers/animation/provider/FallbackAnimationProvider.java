package xyz.nifeather.morph.providers.animation.provider;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.providers.animation.AnimationProvider;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.bundled.FallbackAnimationSet;

public class FallbackAnimationProvider extends AnimationProvider
{
    private final AnimationSet fallback = new FallbackAnimationSet();

    @Override
    public @NotNull AnimationSet getAnimationSetFor(String disguiseID)
    {
        return fallback;
    }
}
