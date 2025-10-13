package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.ISupportDiffs;
import xyz.nifeather.morph.misc.actions.BiConsumerActions;
import xyz.nifeather.morph.misc.disguiseProperty.values.AbstractProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class PropertyHandler
{
    private final Map<SingleProperty<?>, Object> propertyMap = new ConcurrentHashMap<>();
    private final List<SingleProperty<?>> validProperties = new CopyOnWriteArrayList<>();

    protected final BiConsumerActions<SingleProperty<?>, Object> actions = new BiConsumerActions<>();
    public <X> void hookOnPropertyWrite(BiConsumer<SingleProperty<X>, X> consumer)
    {
        actions.hook((BiConsumer) consumer);
    }

    public Map<String, String> toNetworkProperties()
    {
        Map<String, String> map = new ConcurrentHashMap<>();

        try
        {
            for (Map.Entry<SingleProperty<?>, Object> entry : this.propertyMap.entrySet())
            {
                var property = (SingleProperty<Object>) entry.getKey();
                var value = entry.getValue();

                map.put(property.id(), property.forValue(value));
            }

        }
        catch (ParseErrorException e)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().error("Failed writing full network map, some properties may not be synced!", e);
        }

        return map;
    }

    @Nullable
    private AbstractProperties<?> bindingProperties;

    @Nullable
    public AbstractProperties<?> bindingProperties()
    {
        return bindingProperties;
    }

    public void initProperties(AbstractProperties<?> properties)
    {
        reset();

        this.bindingProperties = properties;
        validProperties.addAll(properties.getRegisteredProperties().values());
    }

    public void updateFromPropertiesInput(Map<String, String> input) throws ParseErrorException, PropertyValidationException
    {
        updateFromPropertiesInput(input, map -> {});
    }

    public void updateFromPropertiesInput(Map<String, String> input, IPropertyValidateHandle validateHandle) throws ParseErrorException, PropertyValidationException
    {
        if (this.bindingProperties == null)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("Trying to update property input while a PropertyHandler has not been initialized?!");
            return;
        }

        var results = this.bindingProperties.readFromPropertiesInput(input);
        validateHandle.validate(results);

        results.forEach(this::writeGeneric);
    }

    public void reset()
    {
        this.validProperties.clear();
        this.bindingProperties = null;
        clearProperties();
    }

    public void clearProperties()
    {
        propertyMap.clear();
    }

    private void writeGeneric(SingleProperty<?> property, Object value)
    {
        if (!property.type().isInstance(value))
            throw new IllegalArgumentException("Incompatible value for id '%s', excepted for '%s', but got '%s'".formatted(property.id(), property.defaultVal().getClass(), value.getClass()));

        set((SingleProperty<Object>)property, value);
    }

    /**
     * @throws NullPointerException If the given value is NULL
     */
    public <X> void set(SingleProperty<X> property, @NotNull X value) throws NullPointerException
    {
        if (!validProperties.contains(property))
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("The given property '%s' doesn't exist in '%s'".formatted(property.id(), this.bindingProperties));
            return;
        }

        Objects.requireNonNull(value, "Null values are not accepted");

        var existing = getOptional(property).orElse(null);

        if (!value.equals(existing))
        {
            X diffIfPossible;
            if (existing instanceof ISupportDiffs<?> existingDiff)
                diffIfPossible = ((ISupportDiffs<X>)existingDiff).diff(value);
            else
                diffIfPossible = value;

            propertyMap.put(property, value);
            this.actions.invoke(BiConsumerActions.pair(property, diffIfPossible));
        }
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

    public <X> Optional<X> getOptional(SingleProperty<X> property)
    {
        return Optional.ofNullable(getOr(property, null));
    }

    @Nullable
    @Contract("_, null -> _; _, !null -> !null")
    public <X> X getOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) propertyMap.getOrDefault(property, defaultVal);
    }

    @Nullable
    @Contract("_, null -> _; _, !null -> !null")
    public <X> X getOr(String propertyName, @Nullable X defaultVal)
    {
        var property = validProperties.stream().filter(p -> p.id().equals(propertyName))
                .findFirst()
                .orElse(null);

        if (property == null) return defaultVal;

        return (X) getOr((SingleProperty<Object>) property, defaultVal);
    }
    public Map<SingleProperty<?>, ?> getAll()
    {
        return new Object2ObjectArrayMap<>(propertyMap);
    }

    /**
     * Execute a simple copy which copies our value to the given PropertyHandler
     */
    public void copyTo(PropertyHandler other)
    {
        this.propertyMap.forEach((k, v) ->
        {
            other.set((SingleProperty<Object>) k, v);
        });
    }

    /**
     * Check if the given PropertyHandler has been set up with the same properties of this handler
     */
    public boolean bindingPropertiesEquals(PropertyHandler other)
    {
        return other.bindingProperties != null && this.bindingPropertiesEquals(other.bindingProperties);
    }

    /**
     * Check if the given Properties is same properties of this handler
     */
    public boolean bindingPropertiesEquals(AbstractProperties<?> other)
    {
        return this.bindingProperties != null && this.bindingProperties.equals(other);
    }

    public void dispose()
    {
        actions.clear();
    }
}
