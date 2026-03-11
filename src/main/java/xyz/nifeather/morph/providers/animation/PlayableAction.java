package xyz.nifeather.morph.providers.animation;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public record PlayableAction(
        List<ActionStage> animations
)
{
    public static PlayableActionBuilder builder()
    {
        return new PlayableActionBuilder();
    }

    public static class ActionStageBuilder
    {
        private static final Consumer<DisguiseState> noOp = s -> {};

        private int duration = 0;
        private Consumer<DisguiseState> onPlay = noOp;
        private Consumer<DisguiseState> onFinish = noOp;

        @Nullable
        private String legacyName;

        public ActionStageBuilder duration(int duration)
        {
            this.duration = duration;
            return this;
        }

        public ActionStageBuilder onPlay(Consumer<DisguiseState> onPlay)
        {
            this.onPlay = onPlay;
            return this;
        }

        public ActionStageBuilder onFinish(Consumer<DisguiseState> onFinish)
        {
            this.onFinish = onFinish;
            return this;
        }

        public ActionStageBuilder legacyName(String name)
        {
            this.legacyName = name;
            return this;
        }

        public ActionStage build()
        {
            if (duration < 0)
                throw new IllegalArgumentException("Duration cannot be negative");

            Objects.requireNonNull(onPlay, "action doesn't have a play action.");
            Objects.requireNonNull(onFinish, "action doesn't have a finish action.");
            return new ActionStage(duration, legacyName, onPlay, onFinish);
        }
    }

    public static class PlayableActionBuilder
    {
        private final List<ActionStage> actions = new ObjectArrayList<>();

        public PlayableActionBuilder addStage(Consumer<ActionStageBuilder> consumer)
        {
            var builder = new ActionStageBuilder();
            consumer.accept(builder);

            this.actions.add(builder.build());

            return this;
        }

        public PlayableAction build()
        {
            return new PlayableAction(ImmutableList.copyOf(this.actions));
        }
    }
}
