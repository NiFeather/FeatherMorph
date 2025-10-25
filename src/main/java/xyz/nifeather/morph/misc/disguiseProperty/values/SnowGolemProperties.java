package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class SnowGolemProperties extends BaseLivingEntityProperties<Snowman>
{
    public final SingleProperty<Boolean> HAS_PUMPKIN = createProperty(PropertyNames.SNOW_GOLEM_HAS_PUMPKIN, true, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public SnowGolemProperties()
    {
        registerSingle(HAS_PUMPKIN);
    }

    @Override
    protected @Nullable Snowman tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Snowman snowman ? snowman : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Snowman targetEntity)
    {
        propertyHandler.set(HAS_PUMPKIN, !targetEntity.isDerp());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
