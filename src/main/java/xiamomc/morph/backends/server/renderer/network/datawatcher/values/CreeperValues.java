package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class CreeperValues extends MonsterValues
{
    public final SingleValue<Integer> STATE = getSingle("creeper_state", 0, EntityDataTypes.INT);
    public final SingleValue<Boolean> IS_CHARGED_CREEPER = getSingle("creeper_is_charged", false, EntityDataTypes.BOOLEAN).withRandom(false, false, false, false, true);
    public final SingleValue<Boolean> IGNITED = getSingle("creeper_ignited", false, EntityDataTypes.BOOLEAN);

    public CreeperValues()
    {
        registerSingle(STATE, IS_CHARGED_CREEPER, IGNITED);
    }
}
