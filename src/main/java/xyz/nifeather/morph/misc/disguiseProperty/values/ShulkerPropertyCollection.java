package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class ShulkerPropertyCollection extends BaseLivingEntityPropertyCollection<Shulker>
{
    public final SingleProperty<DyeColor> DYE_COLOR = SingleProperty.builder(PropertyNames.SHULKER_COLOR, DyeColor.class, DyeColor.BLACK)
            .withInputHandle(InputHandles::readDyeColor)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
            .build();

    @ApiStatus.Experimental
    public final SingleProperty<Byte> SHELL_HEIGHT = SingleProperty.builder(PropertyNames.SHULKER_SHELL_HEIGHT, (byte)0)
            .withInputHandle(((propertyName, input) ->
            {
                var val = InputHandles.readByte(propertyName, input);
                if (val.isEmpty()) return Optional.empty();

                InputHandles.throwIfOutOfBounds(propertyName, (int)val.get(), 0, 100);

                return val;
            }))
            .withOutputHandle(OutputHandles::writeByte)
            .build();

    public ShulkerPropertyCollection()
    {
        registerSingle(DYE_COLOR, SHELL_HEIGHT);
    }

    @Override
    protected @Nullable Shulker tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Shulker shulker ? shulker : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Shulker targetEntity)
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
