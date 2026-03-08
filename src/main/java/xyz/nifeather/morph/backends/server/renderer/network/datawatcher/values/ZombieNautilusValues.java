package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariant;
import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariants;

public class ZombieNautilusValues extends AbstractNautilusValues
{
    public final SingleValue<ZombieNautilusVariant> VARIANT = createSingle("zombie_nautilus_variant", ZombieNautilusVariants.TEMPERATE, EntityDataTypes.ZOMBIE_NAUTILUS_VARIANT);

    public ZombieNautilusValues()
    {
        registerSingle(VARIANT);
    }
}
