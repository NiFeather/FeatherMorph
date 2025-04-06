package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractVillagerValues extends AgeableMobValues
{
    public final SingleValue<Integer> HEADSHAKE_TIMER = createSingle("ab_villager_handshake_timer", 0, EntityDataTypes.INT);

    public AbstractVillagerValues()
    {
        super();

        registerSingle(HEADSHAKE_TIMER);
    }
}
