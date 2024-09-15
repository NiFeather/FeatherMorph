package xiamomc.morph.providers.animation.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.providers.animation.AnimationProvider;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.bundled.FallbackAnimationSet;

public class FallbackAnimationProvider extends AnimationProvider
{
    private final AnimationSet fallback = new FallbackAnimationSet();

    @Override
    public @NotNull AnimationSet getAnimationSetFor(String disguiseID)
    {
        return fallback;
    }
}
