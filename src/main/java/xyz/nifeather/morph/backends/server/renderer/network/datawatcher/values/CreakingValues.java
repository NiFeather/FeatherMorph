package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class CreakingValues extends MonsterValues
{
    public final SingleValue<Boolean> CAN_MOVE = createSingle("creaking_can_move", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> IS_ACTIVE = createSingle("creaking_is_active", false, EntityDataTypes.BOOLEAN);

    public CreakingValues()
    {
        registerSingle(CAN_MOVE, IS_ACTIVE);
    }
}
