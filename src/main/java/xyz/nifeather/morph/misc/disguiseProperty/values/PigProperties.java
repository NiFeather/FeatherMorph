package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PigProperties extends AbstractProperties
{
    private final Map<String, Pig.Variant> variantMap = new ConcurrentHashMap<>();

    public final SingleProperty<Pig.Variant> VARIANT;

    public PigProperties()
    {
        for (Pig.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.PIG_VARIANT))
            variantMap.put(variant.key().asString(), variant);

        VARIANT = getSingle("pig_variant", Pig.Variant.TEMPERATE)
                .withRandom(Pig.Variant.TEMPERATE, Pig.Variant.COLD, Pig.Variant.WARM)
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
