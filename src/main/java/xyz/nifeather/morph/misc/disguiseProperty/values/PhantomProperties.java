package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.MathUtils;

import java.util.Map;

public class PhantomProperties extends BaseLivingEntityProperties<Phantom>
{
    public final SingleProperty<Integer> SIZE = getSingle("phantom/size", 1);

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
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(SIZE.id()))
        {
            int size = MathUtils.clamp(1, 10, MathUtils.parseIntOr(value, 1));
            return Pair.of(SIZE, size);
        }

        return super.parseSingleInput(key, value);
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
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                SIZE.id(), propertyHandler.get(SIZE).toString()
        );
    }
}
