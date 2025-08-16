package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CowProperties extends BaseLivingEntityProperties<Cow>
{
    private final Map<String, Cow.Variant> variantMap = new ConcurrentHashMap<>();

    public final SingleProperty<Cow.Variant> VARIANT;

    public CowProperties()
    {
        for (Cow.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.COW_VARIANT))
            variantMap.put(variant.key().asString(), variant);

        VARIANT = getSingle("cow/variant", Cow.Variant.TEMPERATE)
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

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Cow tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Cow cow ? cow : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Cow cow)
    {
        propertyHandler.set(VARIANT, cow.getVariant());
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
