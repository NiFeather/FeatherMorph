package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ZombieNautilus;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public class ZombieNautilusPropertyCollection extends AbstractNautilusPropertyCollection<ZombieNautilus>
{
    public final SingleProperty<ZombieNautilus.Variant> VARIANT = SingleProperty.builder(PropertyNames.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilus.Variant.TEMPERATE)
            .withInputHandle(this::readVariant)
            .withOutputHandle(OutputHandles::writeKeyed)
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
}
