package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.BasePiglinValues;

public class PiglinValues extends BasePiglinValues
{
    public final SingleValue<Boolean> IS_BABY = createSingle("piglin_is_baby", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> CHARGING_CROSSBOW = createSingle("piglin_charging_crossbow", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> DANCING = createSingle("piglin_dancing", false, EntityDataTypes.BOOLEAN);

    public PiglinValues()
    {
        super();

        registerSingle(IS_BABY, CHARGING_CROSSBOW, DANCING);
    }
}
