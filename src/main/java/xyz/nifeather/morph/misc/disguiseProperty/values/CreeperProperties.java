package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

public class CreeperProperties extends BaseLivingEntityProperties<Creeper>
{
    public final SingleProperty<Boolean> CHARGED = SingleProperty.builder(PropertyNames.CREEPER_CHARGED, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withRandom(false, false, false, true)
            .withValidInput("true", "false")
            .build();

    public CreeperProperties()
    {
        registerSingle(CHARGED);
    }

    @Override
    protected @Nullable Creeper tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Creeper creeper ? creeper : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Creeper creeper)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, creeper);

        propertyHandler.set(CHARGED, creeper.isPowered());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CHARGED, DisguiseUtils.pick(CHARGED.randomValues()));
    }

}
