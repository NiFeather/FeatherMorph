package xyz.nifeather.morph.providers.animation;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.function.Consumer;

public record ActionStage(
        int duration,
        @Nullable String legacyName,
        Consumer<DisguiseState> onPlay,
        Consumer<DisguiseState> onFinish
)
{
}
