package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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

    public final SingleProperty<Wolf.Variant> VARIANT = createProperty(PropertyNames.WOLF_VARIANT, Variant.PALE, this::readWolfVariant, OutputHandles::writeKeyed)
            .withRandom(
                    RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT).stream().toList()
            );

    public Optional<Wolf.Variant> readWolfVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.WOLF_VARIANT, propertyName, string);
    }

    public final SingleProperty<UUID> OWNER = createProperty(PropertyNames.WOLF_OWNER, Uuids.NIL_UUID, InputHandles::readUUID, OutputHandles::writeUUID);

    public final SingleProperty<DyeColor> COLLAR_COLOR = createProperty(PropertyNames.WOLF_COLLAR_COLOR, DyeColor.RED, InputHandles::readDyeColor, OutputHandles::writeEnum);

    public WolfProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        COLLAR_COLOR.withValidInput(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList());

        registerSingle(VARIANT, OWNER, COLLAR_COLOR);
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

}
