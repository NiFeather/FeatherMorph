package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public class PhantomPropertyCollection extends BaseLivingEntityPropertyCollection<Phantom>
{
    public final SingleProperty<Integer> SIZE = SingleProperty.builder(PropertyNames.PHANTOM_SIZE, 1)
            .withInputHandle(this::readPhantomSize)
            .withOutputHandle(OutputHandles::writeInteger)
            .build();

    private Optional<Integer> readPhantomSize(String propertyName, String string) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readPhantomSize: Unable to parse phantom size"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 1, 10);

        return Optional.of(val);
    }

    public PhantomPropertyCollection()
    {
        registerSingle(SIZE);
    }

    @Override
    protected @Nullable Phantom tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Phantom phantom ? phantom : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Phantom targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
