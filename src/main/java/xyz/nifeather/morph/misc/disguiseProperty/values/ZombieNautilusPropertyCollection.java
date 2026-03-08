package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.registries.Registries;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ZombieNautilus;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Optional;

public class ZombieNautilusPropertyCollection extends AbstractNautilusPropertyCollection<ZombieNautilus>
{
    public final SingleProperty<ZombieNautilus.Variant> VARIANT = SingleProperty.builder(PropertyNames.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilus.Variant.TEMPERATE)
            .withInputHandle(this::readVariant)
            .withOutputHandle(OutputHandles::writeKeyed)
            .withSuggestions(RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT).stream().map(v -> v.key().asString()).toList())
            .withRandom(RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT).stream().toList())
            .build();

    private Optional<ZombieNautilus.Variant> readVariant(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT, propertyName, input);
    }

    public ZombieNautilusPropertyCollection()
    {
        super();
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable ZombieNautilus tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof ZombieNautilus zn ? zn : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));

        super.setupDefaultProperties(propertyHandler);
    }
}
