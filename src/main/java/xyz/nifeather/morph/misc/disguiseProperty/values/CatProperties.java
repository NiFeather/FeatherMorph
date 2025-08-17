package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
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

    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle(PropertyNames.CAT_VARIANT, Cat.Type.TABBY);
    public final SingleProperty<UUID> OWNER = getSingle(PropertyNames.CAT_OWNER, Uuids.NIL_UUID);

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
        return switch (key)
        {
            case PropertyNames.CAT_VARIANT ->
            {
                var match = variantMap.getOrDefault(value, null);

                if (match != null)
                    yield Pair.of(CAT_VARIANT, match);
                else
                    yield null;
            }

            case PropertyNames.CAT_OWNER ->
            {
                UUID uuid = null;

                try
                {
                    uuid = UUID.fromString(value);
                    yield Pair.of(OWNER, uuid);
                }
                catch (Throwable ignored)
                {
                }

                yield null;
            }

            default -> super.parseSingleInput(key, value);
        };
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
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(CAT_VARIANT.id(), propertyHandler.get(CAT_VARIANT).key().asString());
    }
}
