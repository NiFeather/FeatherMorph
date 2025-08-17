package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;

import java.util.Map;

public class FallbackProperties extends BaseLivingEntityProperties<Entity>
{
    @Override
    protected @Nullable Entity tryCastEntity(@Nullable Entity targetEntity)
    {
        return null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Entity targetEntity)
    {
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
