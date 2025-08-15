package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CatProperties extends BaseLivingEntityProperties<Cat>
{
    private final Map<String, Cat.Type> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Cat.Type variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle("cat_variant", Cat.Type.TABBY);
    public final SingleProperty<UUID> OWNER = getSingle("owner", Uuids.NIL_UUID);

    public CatProperties()
    {
        initVariantMap();
        CAT_VARIANT.withValidInput(variantMap.keySet())
                .withRandom(variantMap.values());

        registerSingle(
                CAT_VARIANT, OWNER
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

        if (key.equals(OWNER.id()))
        {
            UUID uuid = null;

            try
            {
                uuid = UUID.fromString(value);
                return Pair.of(OWNER, uuid);
            }
            catch (Throwable ignored)
            {
            }

            return null;
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Cat tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Cat cat ? cat : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Cat cat)
    {
        propertyHandler.set(CAT_VARIANT, cat.getCatType());

        if (cat.getOwnerUniqueId() != null)
            propertyHandler.set(OWNER, cat.getOwnerUniqueId());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CAT_VARIANT, DisguiseUtils.pick(CAT_VARIANT.getRandomValues()));
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "variant", propertyHandler.get(CAT_VARIANT).key().asString()
        );
    }
}
