package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PigProperties extends BaseLivingEntityProperties<Pig>
{
    private final Map<String, Pig.Variant> variantMap = new ConcurrentHashMap<>();

    public final SingleProperty<Pig.Variant> VARIANT;

    public PigProperties()
    {
        for (Pig.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.PIG_VARIANT))
            variantMap.put(variant.key().asString(), variant);

        VARIANT = SingleProperty.builder(PropertyNames.PIG_VARIANT, Pig.Variant.TEMPERATE)
                .withInputHandle(this::readVariant)
                .withOutputHandle(OutputHandles::writeKeyed)
                .withRandom(Pig.Variant.TEMPERATE, Pig.Variant.COLD, Pig.Variant.WARM)
                .withValidInput(variantMap.keySet())
                .build();

        registerSingle(VARIANT);
    }

    private Optional<Pig.Variant> readVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.PIG_VARIANT, propertyName, string);
    }

    @Override
    protected @Nullable Pig tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Pig pig ? pig : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Pig targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(VARIANT, targetEntity.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }

}
