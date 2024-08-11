package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.MobValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AgeableMobValues extends MobValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle("ageable_mob_is_baby", false, EntityDataTypes.BOOLEAN);

    public AgeableMobValues()
    {
        super();

        registerSingle(IS_BABY);
    }
}
