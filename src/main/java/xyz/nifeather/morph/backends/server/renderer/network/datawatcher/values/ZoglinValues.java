package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class ZoglinValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = createSingle("zoglin_is_baby", false, EntityDataTypes.BOOLEAN);

    public ZoglinValues()
    {
        super();

        registerSingle(IS_BABY);
    }
}
