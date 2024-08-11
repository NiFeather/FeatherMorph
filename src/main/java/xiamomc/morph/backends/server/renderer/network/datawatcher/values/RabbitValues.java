package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class RabbitValues extends AnimalValues
{
    public final SingleValue<Integer> RABBIT_TYPE = getSingle("rabbit_type", 0, EntityDataTypes.INT);

    public RabbitValues()
    {
        super();

        registerSingle(RABBIT_TYPE);
    }
}
