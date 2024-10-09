package xyz.nifeather.morph.providers.animation.provider;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.providers.animation.AnimationProvider;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.bundled.PlayerAnimationSet;

public class PlayerAnimationProvider extends AnimationProvider
{
    private final AnimationSet playerAnimationSet = new PlayerAnimationSet();

    @Override
    public @NotNull AnimationSet getAnimationSetFor(String disguiseID)
    {
        return playerAnimationSet;
    }
}
