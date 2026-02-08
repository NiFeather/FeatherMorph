package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WolfPropertyCollection extends BaseLivingEntityPropertyCollection<Wolf>
{
    private final Map<String, Wolf.Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Wolf.Variant> VARIANT;

    public Optional<Wolf.Variant> readWolfVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.WOLF_VARIANT, propertyName, string);
    }

    public final SingleProperty<UUID> OWNER = SingleProperty.builder(PropertyNames.WOLF_OWNER, Uuids.NIL_UUID)
            .withInputHandle(InputHandles::readUUID)
            .withOutputHandle(OutputHandles::writeUUID)
            .build();

    public final SingleProperty<DyeColor> COLLAR_COLOR;

    public WolfPropertyCollection()
    {
        initMap();
        VARIANT = SingleProperty.builder(PropertyNames.WOLF_VARIANT, Variant.PALE)
                .withInputHandle(this::readWolfVariant)
                .withOutputHandle(OutputHandles::writeKeyed)
                .withRandom(RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT).stream().toList())
                .withSuggestions(variantMap.keySet())
                .build();

        COLLAR_COLOR =  SingleProperty.builder(PropertyNames.WOLF_COLLAR_COLOR, DyeColor.RED)
                .withInputHandle(InputHandles::readDyeColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
                .build();

        registerSingle(VARIANT, OWNER, COLLAR_COLOR);
    }

    @Override
    protected @Nullable Wolf tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Wolf wolf ? wolf : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Wolf targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(VARIANT, targetEntity.getVariant());
        propertyHandler.set(COLLAR_COLOR, targetEntity.getCollarColor());

        if (targetEntity.getOwnerUniqueId() != null)
            propertyHandler.set(OWNER, targetEntity.getOwnerUniqueId());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }
}
