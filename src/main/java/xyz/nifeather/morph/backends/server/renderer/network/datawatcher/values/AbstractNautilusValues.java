package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class AbstractNautilusValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> DASHING = createSingle("abstract_nautilus_dashing", false, EntityDataTypes.BOOLEAN);

    public AbstractNautilusValues()
    {
        registerSingle(DASHING);
    }
}
