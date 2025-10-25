package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;

public class FallbackProperties extends BaseLivingEntityProperties<Entity>
{
    @Override
    protected @Nullable Entity tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Entity targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
