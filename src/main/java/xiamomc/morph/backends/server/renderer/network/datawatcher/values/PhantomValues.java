package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class PhantomValues extends MobValues
{
    public final SingleValue<Integer> SIZE = getSingle("phantom_size", 0, EntityDataTypes.INT);

    public PhantomValues()
    {
        super();

        registerSingle(SIZE);
    }
}
