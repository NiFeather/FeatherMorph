package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;

public class FallbackPropertyCollection extends BaseLivingEntityPropertyCollection<Entity>
{
    @Override
    protected @Nullable Entity tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
