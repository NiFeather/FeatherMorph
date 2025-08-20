package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
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

public class CatProperties extends BaseLivingEntityProperties<Cat>
{
    private final Map<String, Cat.Type> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Cat.Type variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle(PropertyNames.CAT_VARIANT, Cat.Type.TABBY, this::readCatVariant);

    private Optional<Cat.Type> readCatVariant(String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.CAT_VARIANT, string);
    }

    public final SingleProperty<UUID> OWNER = getSingle(PropertyNames.CAT_OWNER, Uuids.NIL_UUID, InputHandles::readUUID);
    public final SingleProperty<DyeColor> COLLAR_COLOR = getSingle(PropertyNames.CAT_COLLAR_COLOR, DyeColor.RED, InputHandles::readDyeColor);

    public CatProperties()
    {
        initVariantMap();
        CAT_VARIANT.withValidInput(variantMap.keySet())
                .withRandom(variantMap.values());

        COLLAR_COLOR.withValidInput(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList());

        registerSingle(
                CAT_VARIANT, OWNER, COLLAR_COLOR
        );
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
