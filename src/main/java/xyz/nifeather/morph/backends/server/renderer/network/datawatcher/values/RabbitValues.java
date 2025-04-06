package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class RabbitValues extends AnimalValues
{
    public final SingleValue<Integer> RABBIT_TYPE = createSingle("rabbit_type", 0, EntityDataTypes.INT);

    public RabbitValues()
    {
        super();

        registerSingle(RABBIT_TYPE);
    }
}
