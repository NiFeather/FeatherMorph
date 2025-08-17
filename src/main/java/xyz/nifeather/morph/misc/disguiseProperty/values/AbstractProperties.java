package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractProperties<E extends Entity>
{
    protected <X> SingleProperty<X> getSingle(String name, X val)
    {
        if (val == null)
            throw new IllegalArgumentException("May not pass a null value to getSingle()");

        return SingleProperty.of(name, val);
    }

    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    protected final List<SingleProperty<?>> values = new CopyOnWriteArrayList<>();

    protected void registerSingle(SingleProperty<?>... value)
    {
        for (SingleProperty<?> property : value)
            registerSingle(property);
    }

    protected void registerSingle(SingleProperty<?> value)
    {
        var duplicateValue = values.stream().filter(p -> p.id().equals(value.id())).findFirst().orElse(null);
        if (duplicateValue != null)
            throw new IllegalArgumentException("Already contains a value with ID '%s'".formatted(value.id()));

        values.add(value);
    }

    public List<SingleProperty<?>> getValues()
    {
        return new ObjectArrayList<>(values);
    }

    @Nullable
    protected abstract Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value);

    public final Map<SingleProperty<?>, Object> readFromPropertiesInput(Map<String, String> propertiesInput)
    {
        var map = new ConcurrentHashMap<SingleProperty<?>, Object>();

        propertiesInput.forEach((key, value) ->
        {
            var pair = this.parseSingleInput(key, value);

            if (pair != null)
                map.put(pair.key(), pair.value());
        });

        return map;
    }

    @Nullable
    protected abstract E tryCastEntity(@Nullable Entity targetEntity);

    public final void setupProperties(DisguiseState state, @Nullable Entity targetEntity)
    {
        var cast = tryCastEntity(targetEntity);

        if (cast != null)
            setupPropertiesFromEntity(state.disguisePropertyHandler(), cast);
        else
            setupDefaultProperties(state.disguisePropertyHandler());
    }

    protected abstract void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull E targetEntity);
    protected abstract void setupDefaultProperties(PropertyHandler propertyHandler);

    public final Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        var map = new ConcurrentHashMap<String, String>();
        this.appendNetworkMap(propertyHandler, map);

        return map;
    }

    protected abstract void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map);
}
