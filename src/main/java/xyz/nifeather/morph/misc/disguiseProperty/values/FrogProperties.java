package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrogProperties extends BaseLivingEntityProperties<Frog>
{
    private final Map<String, Frog.Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (Frog.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.FROG_VARIANT).stream().toList())
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Frog.Variant> VARIANT = getSingle("frog/variant", Frog.Variant.TEMPERATE)
            .withRandom(Frog.Variant.TEMPERATE, Frog.Variant.COLD, Frog.Variant.WARM);

    public FrogProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        registerSingle(
                VARIANT
        );
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(VARIANT.id()))
        {
            var match = variantMap.getOrDefault(value, null);

            if (match != null)
                return Pair.of(VARIANT, match);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Frog tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Frog frog ? frog : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Frog frog)
    {
        propertyHandler.set(VARIANT, frog.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                VARIANT.id(), propertyHandler.get(VARIANT).key().asString()
        );
    }
}
