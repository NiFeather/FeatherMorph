package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class GhastValues extends MobValues
{
    public final SingleValue<Boolean> CHARGING = getSingle("ghast_charging", false, EntityDataTypes.BOOLEAN);

    public GhastValues()
    {
        super();

        registerSingle(CHARGING);
    }
}
