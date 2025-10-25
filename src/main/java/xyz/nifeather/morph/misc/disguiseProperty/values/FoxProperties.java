package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Optional;

public class FoxProperties extends BaseLivingEntityProperties<Fox>
{
    public final SingleProperty<Fox.Type> VARIANT = createProperty(PropertyNames.FOX_VARIANT, Fox.Type.RED, this::readFoxType, OutputHandles::writeEnum)
            .withRandom(Fox.Type.values())
            .withValidInput("red", "snow");

    public Optional<Fox.Type> readFoxType(String propertyName, String input) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Fox.Type.values(), propertyName, input);
    }

    public FoxProperties()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Fox tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Fox fox ? fox : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Fox fox)
    {
        propertyHandler.set(VARIANT, fox.getFoxType());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

}
