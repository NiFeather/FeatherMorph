package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class EnderDragonValues extends MobValues
{
    public final SingleValue<Integer> DRAGON_PHASE;

    public EnderDragonValues()
    {
        super();

        DRAGON_PHASE = createSingle("dragon_phase", 10, EntityDataTypes.INT);

        registerSingle(DRAGON_PHASE);
    }
}
