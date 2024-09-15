package xiamomc.morph.providers.animation;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;

public abstract class AnimationProvider extends MorphPluginObject
{
    /**
     * @param disguiseID
     * @return The sequence of the given parameters, {@link xiamomc.morph.providers.animation.bundled.FallbackAnimationSet} if invalid.
     */
    @NotNull
    public abstract AnimationSet getAnimationSetFor(String disguiseID);
}
