package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CowProperties extends BaseLivingEntityProperties<Cow>
{
    private final Map<String, Cow.Variant> variantMap = new ConcurrentHashMap<>();

    public final SingleProperty<Cow.Variant> VARIANT;

    public CowProperties()
    {
        for (Cow.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.COW_VARIANT))
            variantMap.put(variant.key().asString(), variant);

        VARIANT = createProperty(PropertyNames.COW_VARIANT, Cow.Variant.TEMPERATE, this::readCowVariant, OutputHandles::writeKeyed)
                .withRandom(variantMap.values())
                .withValidInput(variantMap.keySet());

        registerSingle(VARIANT);
    }

    private Optional<Cow.Variant> readCowVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.COW_VARIANT, propertyName, string);
    }

    @Override
    protected @Nullable Cow tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Cow cow ? cow : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Cow cow)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, cow);

        propertyHandler.set(VARIANT, cow.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

}
