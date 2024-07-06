package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractFishValues;

public class TropicalFishValues extends AbstractFishValues
{
    public final SingleValue<Integer> FISH_VARIANT = getSingle("tropical_variant", 0, EntityDataTypes.INT);

    public TropicalFishValues()
    {
        super();

        registerSingle(FISH_VARIANT);
    }
}
