package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class SnifferValues extends AnimalValues
{
    public final SingleValue<SnifferState> SNIFFER_STATE = createSingle("sniffer_state", SnifferState.IDLING, EntityDataTypes.SNIFFER_STATE);
    public final SingleValue<Integer> DROP_SEED_AT_TICK = createSingle("drop_at_tick", 0, EntityDataTypes.INT);

    public SnifferValues()
    {
        registerSingle(SNIFFER_STATE, DROP_SEED_AT_TICK);
    }
}
