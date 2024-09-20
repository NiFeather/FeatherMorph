package xiamomc.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
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
        properties.getValues().forEach(this::addProperty);
    }

    private void addProperty(SingleProperty<?> property)
    {
        var random = property.getRandomValues();
        if (!random.isEmpty())
        {
            var index = this.random.nextInt(random.size());
            this.writeGeneric(property, random.get(index));
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
        if (!propertyMap.containsKey(property))
        {
            MorphPlugin.getInstance().getSLF4JLogger().warn("The given property '%s' doesn't exist.".formatted(property));
            return;
        }

        propertyMap.put(property, value);
    }

    @NotNull
    public <X> X get(SingleProperty<X> property)
    {
        return this.getOr(property, property.defaultVal());
    }

    @Nullable
    @Contract("_, null -> null; _, !null -> !null")
    public <X> X getOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) propertyMap.getOrDefault(property, defaultVal);
    }

    public Map<SingleProperty<?>, ?> getAll()
    {
        return new Object2ObjectArrayMap<>(propertyMap);
    }
}
