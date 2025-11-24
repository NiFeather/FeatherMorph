package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

public class GoatPropertyCollection extends BaseLivingEntityPropertyCollection<Goat>
{
    public final SingleProperty<Boolean> HAS_LEFT_HORN = SingleProperty.builder(PropertyNames.GOAT_HAS_LEFT_HORN, true)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withRandom(true, true, true, false)
            .withSuggestions("false", "true")
            .build();

    public final SingleProperty<Boolean> HAS_RIGHT_HORN = SingleProperty.builder(PropertyNames.GOAT_HAS_RIGHT_HORN, true)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withRandom(true, true, true, false)
            .withSuggestions("false", "true")
            .build();

    public GoatPropertyCollection()
    {
        registerSingle(HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }

    @Override
    protected @Nullable Goat tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Goat goat ? goat : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Goat goat)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, goat);

        propertyHandler.set(HAS_LEFT_HORN, goat.hasLeftHorn());
        propertyHandler.set(HAS_RIGHT_HORN, goat.hasRightHorn());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(HAS_RIGHT_HORN, DisguiseUtils.pick(HAS_RIGHT_HORN.randomValues()));
        propertyHandler.set(HAS_LEFT_HORN, DisguiseUtils.pick(HAS_LEFT_HORN.randomValues()));
    }

}
