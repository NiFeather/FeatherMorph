package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChickenProperties extends BaseLivingEntityProperties<Chicken>
{
    private final Map<String, Chicken.Variant> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (var variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CHICKEN_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Chicken.Variant> VARIANT = getSingle("chicken_variant", Chicken.Variant.TEMPERATE);

    public ChickenProperties()
    {
        initVariantMap();
        VARIANT.withValidInput(variantMap.keySet())
                .withRandom(variantMap.values());

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
    protected @Nullable Chicken tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Chicken chicken ? chicken : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Chicken chicken)
    {
        propertyHandler.set(VARIANT, chicken.getVariant());
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
                "variant", propertyHandler.get(VARIANT).key().asString()
        );
    }
}
