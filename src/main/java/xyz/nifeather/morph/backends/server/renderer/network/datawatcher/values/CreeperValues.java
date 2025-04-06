package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class CreeperValues extends MonsterValues
{
    public final SingleValue<Integer> STATE = createSingle("creeper_state", 0, EntityDataTypes.INT);
    public final SingleValue<Boolean> IS_CHARGED_CREEPER = createSingle("creeper_is_charged", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> IGNITED = createSingle("creeper_ignited", false, EntityDataTypes.BOOLEAN);

    public CreeperValues()
    {
        registerSingle(STATE, IS_CHARGED_CREEPER, IGNITED);
    }
}
