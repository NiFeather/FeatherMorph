package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitProperties extends BaseLivingEntityProperties<Rabbit>
{
    private final Map<String, Type> typeMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var type : Type.values())
            typeMap.put(type.name().toLowerCase(), type);
    }

    public final SingleProperty<Rabbit.Type> VARIANT = getSingle("rabbit/variant", Type.BROWN)
            .withRandom(Type.values());

    public RabbitProperties()
    {
        initMap();
        VARIANT.withValidInput(typeMap.keySet());

        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(VARIANT.id()))
        {
            var variant = typeMap.getOrDefault(value, null);

            if (variant != null)
                return Pair.of(VARIANT, variant);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Rabbit tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Rabbit rabbit ? rabbit : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Rabbit targetEntity)
    {
        propertyHandler.set(VARIANT, targetEntity.getRabbitType());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(VARIANT.id(), propertyHandler.get(VARIANT).name().toLowerCase());
    }
}
