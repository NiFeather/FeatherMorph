package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Arrays;
import java.util.Map;

public class ShulkerProperties extends BaseLivingEntityProperties<Shulker>
{
    public final SingleProperty<DyeColor> DYE_COLOR = getSingle(PropertyNames.SHULKER_COLOR, DyeColor.getByWoolData((byte)15), InputHandles::readDyeColor);

    public ShulkerProperties()
    {
        DYE_COLOR.withValidInput(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList());
        registerSingle(DYE_COLOR);
    }

    @Override
    protected @Nullable Shulker tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Shulker shulker ? shulker : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Shulker targetEntity)
    {
        if (targetEntity.getColor() != null)
            propertyHandler.set(DYE_COLOR, targetEntity.getColor());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        propertyHandler.getOptional(DYE_COLOR).ifPresent(v -> map.put(DYE_COLOR.id(), v.name().toLowerCase()));
    }
}
