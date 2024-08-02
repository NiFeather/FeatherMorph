package xiamomc.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.disguiseProperty.values.AbstractProperties;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PropertyHandler
{
    private final Map<SingleProperty<?>, Object> propertyMap = new ConcurrentHashMap<>();

    private final Random random = ThreadLocalRandom.current();

    public void setProperties(AbstractProperties properties)
    {
        reset();

        for (SingleProperty<?> property : properties.getValues())
        {
            var random = property.getRandomValues();
            if (!random.isEmpty())
            {
                var index = this.random.nextInt(random.size());
                this.writeGeneric(property, random.get(index));
            }
        }
    }

    public void reset()
    {
        propertyMap.clear();
    }

    private void writeGeneric(SingleProperty<?> property, Object value)
    {
        if (!property.defaultVal().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatable value for id '%s', excepted for '%s', but got '%s'".formatted(property.id(), property.defaultVal().getClass(), value.getClass()));

        set((SingleProperty<Object>)property, value);
    }

    public <X> void set(SingleProperty<X> property, X value)
    {
        propertyMap.put(property, value);
    }

    @Nullable
    public <X> X get(SingleProperty<X> property)
    {
        return (X) propertyMap.getOrDefault(property, property.defaultVal());
    }

    public Map<SingleProperty<?>, ?> getAll()
    {
        return new Object2ObjectArrayMap<>(propertyMap);
    }
}
