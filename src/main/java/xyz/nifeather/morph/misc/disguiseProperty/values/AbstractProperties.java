package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProperties<E extends Entity>
{
    protected <X> SingleProperty<X> createProperty(String name, X val, InputHandle<X> inputHandle, OutputHandle<X> outputHandle)
    {
        if (val == null)
            throw new IllegalArgumentException("May not pass a null value to getSingle()");

        return SingleProperty.of(name, val, inputHandle, outputHandle);
    }

    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    protected final Map<String, SingleProperty<?>> values = new ConcurrentHashMap<>();

    protected void registerSingle(SingleProperty<?>... value)
    {
        for (SingleProperty<?> property : value)
            registerSingle(property);
    }

    protected void registerSingle(SingleProperty<?> value)
    {
        var duplicateValue = values.getOrDefault(value.id(), null);
        if (duplicateValue != null)
            throw new IllegalArgumentException("Already contains a value with ID '%s'".formatted(value.id()));

        values.put(value.id(), value);
    }

    @Deprecated
    public List<SingleProperty<?>> getValues()
    {
        return new ObjectArrayList<>(values.values());
    }

    public Map<String, SingleProperty<?>> getRegisteredProperties()
    {
        return Map.copyOf(this.values);
    }

    public final Map<SingleProperty<?>, Object> readFromPropertiesInput(Map<String, String> propertiesInput) throws ParseErrorException
    {
        var map = new ConcurrentHashMap<SingleProperty<?>, Object>();

        for (Map.Entry<String, String> entry : propertiesInput.entrySet())
        {
            var key = entry.getKey();
            var value = entry.getValue();

            var property = (SingleProperty<Object>) this.values.getOrDefault(key, null);
            if (property == null)
                continue;

            property.forInput(value).ifPresent(o -> map.put(property, o));
        }

        return map;
    }

    @Nullable
    protected abstract E tryCastEntity(@Nullable Entity targetEntity);

    protected boolean validateOtherDisguise(DisguiseState our, DisguiseState other)
    {
        return our.disguisePropertyHandler().bindingPropertiesEquals(other.disguisePropertyHandler());
    }

    public final void setupProperties(DisguiseState state, @Nullable Entity targetEntity)
    {
        var theirDisguise = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager()
                .getDisguiseStateFor(targetEntity);

        // Clone if the target entity is disguised.
        // If their disguise is not compatible with ours, we don't want to continue cloning from them anyway
        if (theirDisguise != null)
        {
            if (validateOtherDisguise(state, theirDisguise))
                this.setupFromOtherDisguise(state, theirDisguise);

            return;
        }

        var cast = tryCastEntity(targetEntity);

        if (cast != null)
            setupPropertiesFromEntity(state.disguisePropertyHandler(), cast);
        else
            setupDefaultProperties(state.disguisePropertyHandler());
    }

    protected abstract void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull E targetEntity);
    protected abstract void setupDefaultProperties(PropertyHandler propertyHandler);

    protected void setupFromOtherDisguise(DisguiseState ourState, DisguiseState theirState)
    {
        var ourHandler = ourState.disguisePropertyHandler();
        var theirHandler = theirState.disguisePropertyHandler();

        theirHandler.copyTo(ourHandler);
    }

    //todo: We might not want ParseErrorException to be caught here
    public final Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        var map = new ConcurrentHashMap<String, String>();

        try
        {
            for (SingleProperty<?> p : values.values())
            {
                SingleProperty<Object> property = (SingleProperty<Object>) p;

                var optional = propertyHandler.getOptional(property);
                if (optional.isPresent())
                    map.put(property.id(), property.forValue(optional.get()));
            }
        }
        catch (ParseErrorException e)
        {
            logger.error("Failed writing values", e);
        }

        return map;
    }
}
