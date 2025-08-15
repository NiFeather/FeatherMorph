package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.disguiseProperty.values.AbstractProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PropertyHandler
{
    private final Map<SingleProperty<?>, Object> propertyMap = new ConcurrentHashMap<>();
    private final List<SingleProperty<?>> validProperties = new CopyOnWriteArrayList<>();

    public Map<String, String> toNetworkProperties()
    {
        if (bindingProperties == null)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("Trying to map network properties while a PropertyHandler has not been initialized?!");

            return new HashMap<>();
        }

        return bindingProperties.mapToNetworkProperties(this);
    }

    @Nullable
    private AbstractProperties<?> bindingProperties;

    public void initProperties(AbstractProperties<?> properties)
    {
        reset();

        this.bindingProperties = properties;
        validProperties.addAll(properties.getValues());
    }

    public void updateFromPropertiesInput(Map<String, String> input)
    {
        if (this.bindingProperties == null)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("Trying to update property input while a PropertyHandler has not been initialized?!");
            return;
        }

        var results = this.bindingProperties.readFromPropertiesInput(input);
        results.forEach(this::writeGeneric);
    }

    public void reset()
    {
        this.validProperties.clear();
        this.bindingProperties = null;
        propertyMap.clear();
    }

    private void writeGeneric(SingleProperty<?> property, Object value)
    {
        if (!property.defaultVal().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatible value for id '%s', excepted for '%s', but got '%s'".formatted(property.id(), property.defaultVal().getClass(), value.getClass()));

        set((SingleProperty<Object>)property, value);
    }

    public <X> void set(SingleProperty<X> property, X value)
    {
        if (!validProperties.contains(property))
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("The given property '%s' doesn't exist in '%s'".formatted(property.id(), this.bindingProperties));
            return;
        }

        propertyMap.put(property, value);
    }

    public boolean contains(SingleProperty<?> property)
    {
        return propertyMap.containsKey(property);
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
