package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChickenProperties extends BaseLivingEntityProperties<Chicken>
{
    private final Map<String, Chicken.Variant> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (var variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.CHICKEN_VARIANT))
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Chicken.Variant> VARIANT = getSingle(PropertyNames.CHICKEN_VARIANT, Chicken.Variant.TEMPERATE, this::readChickenVariant);

    private Optional<Chicken.Variant> readChickenVariant(String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.CHICKEN_VARIANT, string);
    }

    public ChickenProperties()
    {
        initVariantMap();
        VARIANT.withValidInput(variantMap.keySet())
                .withRandom(variantMap.values());

        registerSingle(
                VARIANT
        );
    }

    @Override
    protected @Nullable Chicken tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Chicken chicken ? chicken : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Chicken chicken)
    {
        propertyHandler.set(VARIANT, chicken.getVariant());
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
