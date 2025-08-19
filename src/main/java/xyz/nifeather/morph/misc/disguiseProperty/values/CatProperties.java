package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Cat;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatProperties extends AbstractProperties
{
    private final Map<String, Cat.Type> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Cat.Type variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle("cat_variant", Cat.Type.TABBY);

    public CatProperties()
    {
        initVariantMap();
        CAT_VARIANT.withValidInput(variantMap.keySet())
                .withRandom(variantMap.values());

        registerSingle(
                CAT_VARIANT
        );
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(CAT_VARIANT.id()))
        {
            var match = variantMap.getOrDefault(value, null);

            if (match != null)
                return Pair.of(CAT_VARIANT, match);
        }

        return null;
    }
}
