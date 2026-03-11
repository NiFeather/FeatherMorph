package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.ISupportDiffs;
import xyz.nifeather.morph.misc.actions.BiConsumerActions;
import xyz.nifeather.morph.misc.disguiseProperty.values.PropertyCollection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class PropertyHandler
{
    private final Map<SingleProperty<?>, Object> propertyMap = new ConcurrentHashMap<>();
    private final Map<String, SingleProperty<?>> validProperties = new ConcurrentHashMap<>();

    protected final BiConsumerActions<SingleProperty<?>, Object> actions = new BiConsumerActions<>();
    public <X> void hookOnPropertyWrite(BiConsumer<SingleProperty<X>, X> consumer)
    {
        actions.hook((BiConsumer) consumer);
    }

    protected final BiConsumerActions<SingleProperty<?>, Object> discardHooks = new BiConsumerActions<>();
    public <X> void hookOnPropertyDiscard(BiConsumer<SingleProperty<X>, X> consumer)
    {
        discardHooks.hook((BiConsumer) consumer);
    }

    public Map<String, String> toNetworkProperties()
            throws ParseErrorException, ExecutionErrorException
    {
        Map<String, String> map = new ConcurrentHashMap<>();

        try
        {
            for (Map.Entry<SingleProperty<?>, Object> entry : this.propertyMap.entrySet())
            {
                var property = (SingleProperty<Object>) entry.getKey();

                // Skip properties that's not visible to client
                if (property.hideFromClient())
                    continue;

                var value = entry.getValue();

                map.put(property.id(), property.forValue(value));
            }
        }
        catch (ParseErrorException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            throw ExecutionErrorException.forMethod("PropertyHandler#toNetworkProperties")
                    .withMessage("Unhandled exception.")
                    .causedBy(t)
                    .create();
        }

        return map;
    }

    public void registerFromPropertyCollection(PropertyCollection<?> properties)
    {
        validProperties.putAll(properties.getRegisteredProperties());
    }

    /**
     * Add property as a valid property for this handler
     * Kept for external use, so that if anyone wants to add their own property, they can call this method!
     */
    public void addProperty(SingleProperty<?> property)
    {
        validProperties.put(property.id(), property);
    }

    public void updateFromPropertiesInput(Map<String, String> input, Entity inputSource, EnumSet<ValidationFlag> validationFlags)
            throws ParseErrorException, PropertyValidationException
    {
        var parsedResults = new ConcurrentHashMap<SingleProperty<?>, Object>();
        var propertiesToRemove = new ObjectArrayList<SingleProperty<?>>();

        for (Map.Entry<String, String> entry : input.entrySet())
        {
            var key = entry.getKey();
            var value = entry.getValue();

            var property = (SingleProperty<Object>) this.validProperties.getOrDefault(key, null);
            if (property == null)
                continue;

            if (value.equals("!"))
            {
                propertiesToRemove.add(property);
                continue;
            }

            var val = property.forInput(value).orElse(null);
            if (val == null) continue;

            property.validateInput(val, inputSource, validationFlags);
            parsedResults.put(property, val);
            this.writeGeneric(property, val);
        }

        propertiesToRemove.forEach(this::discardProperty);

        for (Map.Entry<SingleProperty<?>, Object> entry : parsedResults.entrySet())
        {
            var property = (SingleProperty<Object>) entry.getKey();
            var value = entry.getValue();

            property.postProcessHandle().handle(value, this);
        }
    }

    /**
     * Discard the property, remove its value from this PropertyHandler
     */
    public void discardProperty(SingleProperty<?> property)
    {
        var existingValue = propertyMap.remove(property);
        if (existingValue == null) // It doesn't even exist, don't trigger the action.
            return;

        discardHooks.invoke(Pair.of(property, null));
    }

    public void reset()
    {
        this.validProperties.clear();
        clearProperties();
    }

    public void clearProperties()
    {
        propertyMap.clear();
    }

    private void writeGeneric(SingleProperty<?> property, Object value)
    {
        if (!property.type().isInstance(value))
            throw new IllegalArgumentException("Incompatible value for id '%s', excepted for '%s', but got '%s'".formatted(property.id(), property.type(), value.getClass()));

        set((SingleProperty<Object>)property, value);
    }

    /**
     * @throws NullPointerException If the given value is NULL
     */
    public <X> void set(SingleProperty<X> property, @NotNull X value) throws NullPointerException
    {
        if (!validProperties.containsKey(property.id()))
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().warn("The given property '%s' is not registered in propertyHandler".formatted(property.id()));
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

    public boolean contains(String propertyName)
    {
        return propertyMap.keySet().stream().anyMatch(sp -> sp.id().equals(propertyName));
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
        var property = validProperties.getOrDefault(propertyName, null);
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
        other.validProperties.putAll(this.validProperties);
        this.propertyMap.forEach((k, v) -> other.set((SingleProperty<Object>) k, v));
    }

    public void dispose()
    {
        actions.clear();
    }
}
