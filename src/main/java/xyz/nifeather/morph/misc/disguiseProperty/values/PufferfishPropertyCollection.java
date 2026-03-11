package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.PufferFish;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class PufferfishPropertyCollection extends BaseLivingEntityPropertyCollection<PufferFish>
{
    public final SingleProperty<PufferfishState> PUFF_STATE = SingleProperty.builder(PropertyNames.PUFFERFISH_PUFF_STATE, PufferfishState.SMALL)
            .withInputHandle(this::readPufferfishState)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(PufferfishState.values()).map(s -> s.name().toLowerCase()).toList())
            .build();

    private Optional<PufferfishState> readPufferfishState(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(PufferfishState.values(), propertyName, input);
    }

    public PufferfishPropertyCollection()
    {
        registerSingle(PUFF_STATE);
    }

    @Override
    protected @Nullable PufferFish tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof PufferFish pufferFish ? pufferFish : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    public enum PufferfishState
    {
        SMALL,
        MID,
        LARGE
    }
}
