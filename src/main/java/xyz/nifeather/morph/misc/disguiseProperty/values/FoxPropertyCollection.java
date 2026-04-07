package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.Optional;

public class FoxPropertyCollection extends BaseLivingEntityPropertyCollection<Fox>
{
    public final SingleProperty<Fox.Type> VARIANT = SingleProperty.builder(PropertyNames.FOX_VARIANT, Fox.Type.RED)
            .withInputHandle(this::readFoxType)
            .withOutputHandle(OutputHandles::writeEnum)
            .withRandom(Fox.Type.values())
            .withSuggestions("red", "snow")
            .build();

    @ApiStatus.Experimental
    public final SingleProperty<FoxStatus> STATUS = SingleProperty.builder(PropertyNames.FOX_STATUS, FoxStatus.STANDING)
            .withInputHandle(this::readFoxStatus)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(FoxStatus.values()).map(s -> s.name().toLowerCase()).toList())
            .build();

    private Optional<FoxStatus> readFoxStatus(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(FoxStatus.values(), propertyName, input);
    }

    @ApiStatus.Experimental
    public enum FoxStatus
    {
        STANDING,
        SITTING,
        SLEEPING
    }

    public Optional<Fox.Type> readFoxType(String propertyName, String input) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Fox.Type.values(), propertyName, input);
    }

    public FoxPropertyCollection()
    {
        registerSingle(VARIANT, STATUS);
    }

    @Override
    protected @Nullable Fox tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Fox fox ? fox : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Fox fox)
    {
        super.setupPropertiesFromEntity(propertyHandler, fox);

        propertyHandler.set(VARIANT, fox.getFoxType());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }

}
