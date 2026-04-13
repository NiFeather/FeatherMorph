package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zoglin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class ZoglinPropertyCollection extends BaseLivingEntityPropertyCollection<Zoglin>
{
    public final SingleProperty<Boolean> IS_BABY = SingleProperty.builder(PropertyNames.ZOGLIN_IS_BABY, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public ZoglinPropertyCollection()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Zoglin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Zoglin hoglin ? hoglin : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Zoglin targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
