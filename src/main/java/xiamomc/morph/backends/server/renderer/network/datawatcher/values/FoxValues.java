package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

import java.util.UUID;

public class FoxValues extends AnimalValues
{
    public final SingleValue<Integer> FOX_VARIANT = getSingle("fox_variant", 0, EntityDataTypes.INT).withRandom(0, 1);
    public final SingleValue<Byte> FLAGS = getSingle("fox_flags", (byte)0, EntityDataTypes.BYTE);
    //public final SingleValue<UUID> TRUSTED_ID_0 = getSingle("fox_trusted_0", UUID.randomUUID(), EntityDataTypes.OPTIONAL_UUID);
    //public final SingleValue<UUID> TRUSTED_ID_1 = getSingle("fox_trusted_1", UUID.randomUUID(), EntityDataTypes.OPTIONAL_UUID);

    public FoxValues()
    {
        super();

        registerSingle(FOX_VARIANT, FLAGS);
    }
}
