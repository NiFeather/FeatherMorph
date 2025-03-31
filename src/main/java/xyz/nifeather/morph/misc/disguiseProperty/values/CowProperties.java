package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Cow;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CowProperties extends AbstractProperties
{
    private final Map<String, Cow.Variant> variantMap = new ConcurrentHashMap<>();

    public final SingleProperty<Cow.Variant> VARIANT;

    public CowProperties()
    {
        for (Cow.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.COW_VARIANT))
            variantMap.put(variant.key().asString(), variant);

        VARIANT = getSingle("cow_variant", Cow.Variant.TEMPERATE)
                .withRandom(variantMap.values())
                .withValidInput(variantMap.keySet());

        registerSingle(VARIANT);
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
