package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class HoglinPropertyCollection extends BaseLivingEntityPropertyCollection<Hoglin>
{
    public final SingleProperty<Boolean> IS_BABY = SingleProperty.builder(PropertyNames.HOGLIN_IS_BABY, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public HoglinPropertyCollection()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Hoglin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Hoglin hoglin ? hoglin : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Hoglin targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
