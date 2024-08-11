package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractFishValues extends WaterAnimalValues
{
    public final SingleValue<Boolean> FROM_BUCKET = getSingle("ab_fish_from_bucket", false, EntityDataTypes.BOOLEAN);

    public AbstractFishValues()
    {
        super();

        registerSingle(FROM_BUCKET);
    }
}
