package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractHorseValues extends AgeableMobValues
{
    public final SingleValue<Byte> FLAGS = getSingle("ab_horse_flags", (byte)0, EntityDataTypes.BYTE);

    public AbstractHorseValues()
    {
        super();

        registerSingle(FLAGS);
    }
}
