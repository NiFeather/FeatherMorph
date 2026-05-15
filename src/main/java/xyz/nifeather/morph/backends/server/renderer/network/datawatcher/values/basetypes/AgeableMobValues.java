package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.MobValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AgeableMobValues extends MobValues
{
    public final SingleValue<Boolean> IS_BABY = createSingle("ageable_mob_is_baby", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> AGE_LOCKED = createSingle("ageable_mob_age_locked", false, EntityDataTypes.BOOLEAN);

    public AgeableMobValues()
    {
        super();

        registerSingle(IS_BABY, AGE_LOCKED);
    }
}
