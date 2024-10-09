package xyz.nifeather.morph.providers.animation;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.providers.animation.bundled.FallbackAnimationSet;

public abstract class AnimationProvider extends MorphPluginObject
{
    /**
     * @param disguiseID
     * @return The sequence of the given parameters, {@link FallbackAnimationSet} if invalid.
     */
    @NotNull
    public abstract AnimationSet getAnimationSetFor(String disguiseID);
}
