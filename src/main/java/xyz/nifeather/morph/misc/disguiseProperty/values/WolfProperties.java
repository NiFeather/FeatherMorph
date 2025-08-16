package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WolfProperties extends BaseLivingEntityProperties<Wolf>
{
    private final Map<String, Wolf.Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Wolf.Variant> VARIANT = getSingle("wolf/variant", Variant.PALE)
            .withRandom(
                    RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT).stream().toList()
            );

    public final SingleProperty<UUID> OWNER = getSingle("wolf/owner", Uuids.NIL_UUID);

    public WolfProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        registerSingle(VARIANT, OWNER);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(VARIANT.id()))
        {
            var variant = this.variantMap.getOrDefault(value, null);

            if (variant != null)
                return Pair.of(VARIANT, variant);
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
    protected @Nullable Wolf tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Wolf wolf ? wolf : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Wolf targetEntity)
    {
        propertyHandler.set(VARIANT, targetEntity.getVariant());
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
