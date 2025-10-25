package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Optional;

public class MooshroomProperties extends BaseLivingEntityProperties<MushroomCow>
{
    public final SingleProperty<MushroomCow.Variant> VARIANT = createProperty(PropertyNames.MOOSHROOM_VARIANT, MushroomCow.Variant.RED, this::readVariant, OutputHandles::writeEnum)
            .withRandom(MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.BROWN)
            .withValidInput("red", "brown");

    private Optional<MushroomCow.Variant> readVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(MushroomCow.Variant.values(), propertyName, string);
    }

    public MooshroomProperties()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable MushroomCow tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof MushroomCow mushroomCow ? mushroomCow : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull MushroomCow targetEntity)
    {
        propertyHandler.set(VARIANT, targetEntity.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

}
