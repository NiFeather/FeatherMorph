package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FrogProperties extends BaseLivingEntityProperties<Frog>
{
    private final Map<String, Frog.Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (Frog.Variant variant : RegistryAccess.registryAccess().getRegistry(RegistryKey.FROG_VARIANT).stream().toList())
            variantMap.put(variant.key().asString(), variant);
    }

    public final SingleProperty<Frog.Variant> VARIANT = createProperty(PropertyNames.FROG_VARIANT, Frog.Variant.TEMPERATE, this::readFrogVariant, OutputHandles::writeKeyed)
            .withRandom(Frog.Variant.TEMPERATE, Frog.Variant.COLD, Frog.Variant.WARM);

    private Optional<Frog.Variant> readFrogVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.FROG_VARIANT, propertyName, string);
    }

    public FrogProperties()
    {
        initMap();
        VARIANT.withValidInput(variantMap.keySet());

        registerSingle(
                VARIANT
        );
    }

    @Override
    protected @Nullable Frog tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Frog frog ? frog : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Frog frog)
    {
        propertyHandler.set(VARIANT, frog.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

}
