package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class ZombieProperties extends BaseLivingEntityProperties<Zombie>
{
    public final SingleProperty<Boolean> IS_BABY = createProperty(PropertyNames.ZOMBIE_IS_BABY, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public ZombieProperties()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Zombie tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Zombie zombie ? zombie : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Zombie targetEntity)
    {
        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
