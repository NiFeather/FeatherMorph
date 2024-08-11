package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractHorseValues;

public class HorseValues extends AbstractHorseValues
{
    public final SingleValue<Integer> HORSE_VARIANT = getSingle("horse_variant", 0, EntityDataTypes.INT);

    public HorseValues()
    {
        super();

        registerSingle(HORSE_VARIANT);
    }
}
