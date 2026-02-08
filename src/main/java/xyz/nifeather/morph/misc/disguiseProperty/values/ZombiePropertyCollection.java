package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class ZombiePropertyCollection extends BaseLivingEntityPropertyCollection<Zombie>
{
    public final SingleProperty<Boolean> IS_BABY = SingleProperty.builder(PropertyNames.ZOMBIE_IS_BABY, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public ZombiePropertyCollection()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Zombie tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Zombie zombie ? zombie : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Zombie targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
