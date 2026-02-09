package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SlimeMagmaPropertyCollection extends BaseLivingEntityPropertyCollection<Slime>
{
    public final SingleProperty<Integer> SIZE = SingleProperty.builder(PropertyNames.SLIME_MAGMA_SIZE, 1)
            .withInputHandle(this::readSize)
            .withOutputHandle(OutputHandles::writeInteger)
            .build();

    private Optional<Integer> readSize(String propertyName, String string) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readSize: Unable to parse slime/magma size"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 1, 4);

        return Optional.of(val);
    }

    public SlimeMagmaPropertyCollection()
    {
        registerSingle(SIZE);
    }

    @Override
    protected @Nullable Slime tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Slime slime ? slime : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Slime targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(SIZE, ThreadLocalRandom.current().nextInt(1, 5));
    }
}
