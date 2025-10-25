package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public class PhantomProperties extends BaseLivingEntityProperties<Phantom>
{
    public final SingleProperty<Integer> SIZE = createProperty(PropertyNames.PHANTOM_SIZE, 1, this::readPhantomSize, OutputHandles::writeInteger);

    private Optional<Integer> readPhantomSize(String propertyName, String string) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readPhantomSize: Unable to parse phantom size"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 1, 10);

        return Optional.of(val);
    }

    public PhantomProperties()
    {
        registerSingle(SIZE);
    }

    @Override
    protected @Nullable Phantom tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Phantom phantom ? phantom : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Phantom targetEntity)
    {
        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
