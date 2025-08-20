package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

        VARIANT = getSingle(PropertyNames.PIG_VARIANT, Pig.Variant.TEMPERATE, this::readVariant)
                .withRandom(Pig.Variant.TEMPERATE, Pig.Variant.COLD, Pig.Variant.WARM)
                .withValidInput(variantMap.keySet());

        registerSingle(VARIANT);
    }

    private Optional<Pig.Variant> readVariant(String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.PIG_VARIANT, string);
    }

    @Override
    protected @Nullable Pig tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Pig pig ? pig : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Pig targetEntity)
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
