package xyz.nifeather.morph.misc.disguiseProperty;

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
import xyz.nifeather.morph.misc.actions.ConsumerActions;
import xyz.nifeather.morph.misc.disguiseProperty.values.PropertyCollection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PropertyHandler
{
    private final Map<String, SingleProperty<?>> validProperties = new ConcurrentHashMap<>();

    private final Map<SingleProperty<?>, Object> persistProperties = new ConcurrentHashMap<>();
    private final Map<SingleProperty<?>, Object> tempProperties =  new ConcurrentHashMap<>();

    //region hooks

    protected final BiConsumerActions<SingleProperty<?>, Object> hooksOnTemporaryPropertyWrite = new BiConsumerActions<>();
    public <X> void hookOnTemporaryPropertyWrite(BiConsumer<SingleProperty<X>, X> consumer)
    {
        hooksOnTemporaryPropertyWrite.hook((BiConsumer) consumer);
    }

    protected final ConsumerActions<SingleProperty<?>> hooksOnTemporaryPropertyDiscard = new ConsumerActions<>();
    public <X> void hookOnTemporaryPropertyDiscard(Consumer<SingleProperty<X>> consumer)
    {
        hooksOnTemporaryPropertyDiscard.hook((Consumer) consumer);
    }

    protected final BiConsumerActions<SingleProperty<?>, Object> hooksOnPropertyWrite = new BiConsumerActions<>();
    public <X> void hookOnPropertyWrite(BiConsumer<SingleProperty<X>, X> consumer)
    {
        hooksOnPropertyWrite.hook((BiConsumer) consumer);
    }

    protected final ConsumerActions<SingleProperty<?>> discardHooks = new ConsumerActions<>();
    public <X> void hookOnPropertyDiscard(Consumer<SingleProperty<X>> consumer)
    {
        discardHooks.hook((Consumer) consumer);
    }

    //endregion hooks

    private Map<String, String> generateNetworkPropertiesFrom(Map<SingleProperty<?>, Object> values)
            throws ParseErrorException, ExecutionErrorException
    {
        Map<String, String> map = new ConcurrentHashMap<>();

        try
        {
            for (Map.Entry<SingleProperty<?>, Object> entry : values.entrySet())
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

    /**
     * Creates network map for temp properties
     */
    public Map<String, String> serializeTemporaryProperties()
            throws ParseErrorException, ExecutionErrorException
    {
        return generateNetworkPropertiesFrom(tempProperties);
    }

    /**
     * Creates network map for non-temp properties
     */
    public Map<String, String> serializeNonTempProperties()
            throws ParseErrorException, ExecutionErrorException
    {
        return generateNetworkPropertiesFrom(persistProperties);
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

    public void updateFromPropertiesInput(Map<String, String> input, Entity inputSource, EnumSet<ValidationSkipFlag> validationSkipFlags)
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

            property.validateInput(val, inputSource, validationSkipFlags);
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
     * Discard the temporary property.
     */
    public void discardTemporaryProperty(SingleProperty<?> property)
    {
        var existing = tempProperties.remove(property);
        if (existing == null) return;

        hooksOnTemporaryPropertyDiscard.invoke(property);
    }

    /**
     * Discard the property, remove its value from this PropertyHandler
     */
    public void discardProperty(SingleProperty<?> property)
    {
        var existingValue = persistProperties.remove(property);
        var existingTemp = tempProperties.remove(property);

        if (existingValue == null && existingTemp == null) // It doesn't even exist, don't trigger the action.
            return;

        discardHooks.invoke(property);
    }

    public void reset()
    {
        clearProperties();
    }

    public void clearProperties()
    {
        persistProperties.clear();
        tempProperties.clear();
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

            tempProperties.remove(property);
            persistProperties.put(property, value);
            this.hooksOnPropertyWrite.invoke(BiConsumerActions.pair(property, diffIfPossible));
        }
    }

    public <X> void setTemp(SingleProperty<X> property, @NotNull X value) throws NullPointerException
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

            // `setTemp` only has this differ from `set`... Maybe consider merge these two methods?
            tempProperties.put(property, value);
            this.hooksOnTemporaryPropertyWrite.invoke(BiConsumerActions.pair(property, diffIfPossible));
        }
    }

    public boolean contains(SingleProperty<?> property)
    {
        return persistProperties.containsKey(property) || tempProperties.containsKey(property);
    }

    public boolean contains(String propertyName)
    {
        var combinedStream = Stream.concat(persistProperties.keySet().stream(), tempProperties.keySet().stream());
        return combinedStream.anyMatch(sp -> sp.id().equals(propertyName));
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
        var temp = tempProperties.getOrDefault(property, null);
        if (temp != null)
            return (X) temp;

        return (X) persistProperties.getOrDefault(property, defaultVal);
    }

    @Nullable
    @Contract("_, null -> _; _, !null -> !null")
    public <X> X getOr(String propertyName, @Nullable X defaultVal)
    {
        var property = validProperties.getOrDefault(propertyName, null);
        if (property == null) return defaultVal;

        return (X) getOr((SingleProperty<Object>) property, defaultVal);
    }

    public Map<SingleProperty<?>, ?> getAllTemporary()
    {
        return new Object2ObjectArrayMap<>(tempProperties);
    }

    public Map<SingleProperty<?>, ?> getAll()
    {
        return new Object2ObjectArrayMap<>(persistProperties);
    }

    /**
     * Execute a simple copy which copies our value to the given PropertyHandler
     */
    public void copyTo(PropertyHandler other)
    {
        other.validProperties.putAll(this.validProperties);
        this.persistProperties.forEach((k, v) -> other.set((SingleProperty<Object>) k, v));
        this.tempProperties.forEach((k, v) -> other.setTemp((SingleProperty<Object>) k, v));
    }

    public void dispose()
    {
        hooksOnPropertyWrite.clear();
    }
}
