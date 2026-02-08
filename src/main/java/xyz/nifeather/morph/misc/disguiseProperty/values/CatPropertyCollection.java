package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
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

public class CatPropertyCollection extends BaseLivingEntityPropertyCollection<Cat>
{
    private final Map<String, Cat.Type> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Cat.Type variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Cat.Type> CAT_VARIANT;

    private Optional<Cat.Type> readCatVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.CAT_VARIANT, propertyName, string);
    }

    public final SingleProperty<UUID> OWNER = SingleProperty.builder(PropertyNames.CAT_OWNER, Uuids.NIL_UUID)
            .withInputHandle(InputHandles::readUUID)
            .withOutputHandle(OutputHandles::writeUUID)
            .build();
    public final SingleProperty<DyeColor> COLLAR_COLOR;

    public CatPropertyCollection()
    {
        initVariantMap();

        CAT_VARIANT = SingleProperty.builder(PropertyNames.CAT_VARIANT, Cat.Type.TABBY)
                .withInputHandle(this::readCatVariant)
                .withOutputHandle(OutputHandles::writeKeyed)
                .withSuggestions(variantMap.keySet())
                .withRandom(variantMap.values())
                .build();

        COLLAR_COLOR = SingleProperty.builder(PropertyNames.CAT_COLLAR_COLOR, DyeColor.RED)
                .withInputHandle(InputHandles::readDyeColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
                .build();

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
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Cat cat)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, cat);

        propertyHandler.set(CAT_VARIANT, cat.getCatType());
        propertyHandler.set(COLLAR_COLOR, cat.getCollarColor());

        if (cat.getOwnerUniqueId() != null)
            propertyHandler.set(OWNER, cat.getOwnerUniqueId());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CAT_VARIANT, DisguiseUtils.pick(CAT_VARIANT.randomValues()));
    }

}
