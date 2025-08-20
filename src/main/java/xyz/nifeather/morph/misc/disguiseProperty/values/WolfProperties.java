package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Arrays;
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

    public final SingleProperty<Wolf.Variant> VARIANT = getSingle(PropertyNames.WOLF_VARIANT, Variant.PALE)
            .withRandom(
                    RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT).stream().toList()
            );

    public final SingleProperty<UUID> OWNER = getSingle(PropertyNames.WOLF_OWNER, Uuids.NIL_UUID);

    public final SingleProperty<DyeColor> COLLAR_COLOR = getSingle(PropertyNames.WOLF_COLLAR_COLOR, DyeColor.RED);

    public WolfProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        COLLAR_COLOR.withValidInput(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList());

        registerSingle(VARIANT, OWNER, COLLAR_COLOR);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        return switch (key)
        {
            case PropertyNames.WOLF_VARIANT ->
            {
                var variant = this.variantMap.getOrDefault(value, null);

                if (variant != null)
                    yield Pair.of(VARIANT, variant);
                else
                    yield null;
            }

            case PropertyNames.WOLF_OWNER ->
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

            case PropertyNames.WOLF_COLLAR_COLOR ->
            {
                var match = Arrays.stream(DyeColor.values())
                        .filter(v -> v.name().equalsIgnoreCase(value))
                        .findFirst().orElse(null);

                if (match == null)
                    yield null;

                yield Pair.of(COLLAR_COLOR, match);
            }

            default -> super.parseSingleInput(key, value);
        };
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
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(VARIANT.id(), propertyHandler.get(VARIANT).key().asString());
    }
}
