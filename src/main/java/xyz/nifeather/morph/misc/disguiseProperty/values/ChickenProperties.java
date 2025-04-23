package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Chicken;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChickenProperties extends AbstractProperties
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

        return null;
    }
}
