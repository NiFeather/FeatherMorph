package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Optional;

public class MooshroomPropertyCollection extends BaseLivingEntityPropertyCollection<MushroomCow>
{
    public final SingleProperty<MushroomCow.Variant> VARIANT = SingleProperty.builder(PropertyNames.MOOSHROOM_VARIANT, MushroomCow.Variant.RED)
            .withInputHandle(this::readVariant)
            .withOutputHandle(OutputHandles::writeEnum)
            .withRandom(MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.BROWN)
            .withSuggestions("red", "brown")
            .build();

    private Optional<MushroomCow.Variant> readVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(MushroomCow.Variant.values(), propertyName, string);
    }

    public MooshroomPropertyCollection()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable MushroomCow tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof MushroomCow mushroomCow ? mushroomCow : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull MushroomCow targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(VARIANT, targetEntity.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }

}
