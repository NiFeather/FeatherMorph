package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Sniffer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class SnifferPropertyCollection extends BaseLivingEntityPropertyCollection<Sniffer>
{
    @ApiStatus.Experimental
    public final SingleProperty<Sniffer.State> SNIFFER_STATE = SingleProperty.builder(PropertyNames.SNIFFER_STATE, Sniffer.State.IDLING)
            .withInputHandle(this::readSnifferState)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(Sniffer.State.values()).map(s -> s.name().toLowerCase()).toList())
            .build();

    private Optional<Sniffer.State> readSnifferState(String propertyName, String value)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Sniffer.State.values(), propertyName, value);
    }

    public SnifferPropertyCollection()
    {
        registerSingle(SNIFFER_STATE);
    }

    @Override
    protected @Nullable Sniffer tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof Sniffer sniffer ? sniffer : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
