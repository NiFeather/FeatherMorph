package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ParrotProperties extends BaseLivingEntityProperties<Parrot>
{
    private final Map<String, Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var variant : Variant.values())
            variantMap.put(variant.name().toLowerCase(), variant);
    }

    public final SingleProperty<Parrot.Variant> VARIANT = getSingle(PropertyNames.PARROT_VARIANT, Variant.RED, this::readVariant)
            .withRandom(Variant.values());

    private Optional<Variant> readVariant(String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Variant.values(), string);
    }

    public ParrotProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Parrot tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Parrot parrot ? parrot : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Parrot targetEntity)
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
        map.put(VARIANT.id(), propertyHandler.get(VARIANT).name().toLowerCase());
    }
}
