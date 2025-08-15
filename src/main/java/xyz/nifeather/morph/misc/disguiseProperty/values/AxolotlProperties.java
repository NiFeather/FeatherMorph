package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AxolotlProperties extends BaseLivingEntityProperties<Axolotl>
{
    private final Map<String, Axolotl.Variant> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Axolotl.Variant variant : Axolotl.Variant.values())
            variantMap.put(variant.name().toLowerCase(), variant);
    }

    public final SingleProperty<Axolotl.Variant> VARIANT = getSingle("axolotl_color", Axolotl.Variant.LUCY)
            .withRandom(Axolotl.Variant.values());

    public AxolotlProperties()
    {
        initVariantMap();
        VARIANT.withValidInput(variantMap.keySet());

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
    protected @Nullable Axolotl tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Axolotl axolotl ? axolotl : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Axolotl axolotl)
    {
        propertyHandler.set(VARIANT, axolotl.getVariant());
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
                "variant", propertyHandler.get(VARIANT).name().toLowerCase()
        );
    }
}
