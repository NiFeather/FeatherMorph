package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Optional;

public class PhantomProperties extends BaseLivingEntityProperties<Phantom>
{
    public final SingleProperty<Integer> SIZE = getSingle(PropertyNames.PHANTOM_SIZE, 1, this::readPhantomSize);

    private Optional<Integer> readPhantomSize(String string) throws ParseErrorException
    {
        var val = InputHandles.readInteger(string)
                .orElseThrow(() -> new ParseErrorException("readPhantomSize: Unable to parse phantom size"));

        InputHandles.throwIfOutOfBounds(val, 1, 10);

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
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Phantom targetEntity)
    {
        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(SIZE.id(), propertyHandler.get(SIZE).toString());
    }
}
