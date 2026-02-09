package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;

public class SheepPropertyCollection extends BaseLivingEntityPropertyCollection<Sheep>
{
    public final SingleProperty<DyeColor> DYE_COLOR;

    public SheepPropertyCollection()
    {
        DYE_COLOR = SingleProperty.builder(PropertyNames.SHEEP_COLOR, DyeColor.class, DyeColor.getByWoolData((byte) 15))
                .withInputHandle(InputHandles::readDyeColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
                .build();

        registerSingle(DYE_COLOR);
    }

    @Override
    protected @Nullable Sheep tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Sheep sheep ? sheep : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Sheep targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        if (targetEntity.getColor() != null)
            propertyHandler.set(DYE_COLOR, targetEntity.getColor());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
