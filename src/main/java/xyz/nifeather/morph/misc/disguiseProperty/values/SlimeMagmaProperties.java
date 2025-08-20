package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SlimeMagmaProperties extends BaseLivingEntityProperties<Slime>
{
    public final SingleProperty<Integer> SIZE = getSingle(PropertyNames.SLIME_MAGMA_SIZE, 1, this::readSize);

    private Optional<Integer> readSize(String propertyName, String string) throws ParseErrorException
    {
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readSize: Unable to parse slime/magma size"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 1, 4);

        return Optional.of(val);
    }

    public SlimeMagmaProperties()
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
        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(SIZE, ThreadLocalRandom.current().nextInt(0, 4));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(SIZE.id(), propertyHandler.getOr(SIZE, 1).toString());
    }
}
