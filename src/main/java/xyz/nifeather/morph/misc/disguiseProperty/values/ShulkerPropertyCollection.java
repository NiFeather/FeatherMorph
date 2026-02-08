package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;

public class ShulkerPropertyCollection extends BaseLivingEntityPropertyCollection<Shulker>
{
    public final SingleProperty<DyeColor> DYE_COLOR = SingleProperty.builder(PropertyNames.SHULKER_COLOR, DyeColor.class, DyeColor.getByWoolData((byte) 15))
            .withInputHandle(InputHandles::readDyeColor)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
            .build();

    public ShulkerPropertyCollection()
    {
        registerSingle(DYE_COLOR);
    }

    @Override
    protected @Nullable Shulker tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Shulker shulker ? shulker : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Shulker targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        if (targetEntity.getColor() != null)
            propertyHandler.set(DYE_COLOR, targetEntity.getColor());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
