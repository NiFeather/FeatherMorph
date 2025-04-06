package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class SlimeValues extends MobValues
{
    public final SingleValue<Integer> SIZE = createSingle("slimemagma_size", 1, EntityDataTypes.INT);

    public SlimeValues()
    {
        super();

        registerSingle(SIZE);
    }
}
