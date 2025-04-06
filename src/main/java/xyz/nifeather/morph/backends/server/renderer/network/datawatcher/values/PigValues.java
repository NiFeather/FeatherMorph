package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pig.PigVariant;
import com.github.retrooper.packetevents.protocol.entity.pig.PigVariants;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class PigValues extends AnimalValues
{
    public final SingleValue<Integer> PIG_BOOST_TIME = createSingle("pig_boost_time", 0, EntityDataTypes.INT);
    public final SingleValue<PigVariant> PIG_VARIANT = createSingle("pig_variant", PigVariants.TEMPERATE, EntityDataTypes.PIG_VARIANT);

    public PigValues()
    {
        super();

        registerSingle(PIG_BOOST_TIME, PIG_VARIANT);
    }
}
