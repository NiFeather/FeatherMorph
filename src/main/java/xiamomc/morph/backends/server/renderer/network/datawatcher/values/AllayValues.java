package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class AllayValues extends MobValues
{
    public final SingleValue<Boolean> DANCING = getSingle("allay_dancing", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> CAN_DUPLICATE = getSingle("allay_can_dupe", false, EntityDataTypes.BOOLEAN);

    public AllayValues()
    {
        super();

        this.registerSingle(DANCING);
    }
}