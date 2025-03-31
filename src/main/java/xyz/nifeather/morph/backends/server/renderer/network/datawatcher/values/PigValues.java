package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import org.bukkit.entity.Pig;
import xyz.nifeather.morph.backends.server.renderer.network.CustomSerializeMethods;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class PigValues extends AnimalValues
{
    public final SingleValue<Integer> PIG_BOOST_TIME = createSingle("pig_boost_time", 0);
    public final SingleValue<Pig.Variant> PIG_VARIANT = createSingle("pig_variant", Pig.Variant.TEMPERATE);

    public PigValues()
    {
        super();

        PIG_VARIANT.setSerializeMethod(CustomSerializeMethods.PIG_VARIANT);

        registerSingle(PIG_BOOST_TIME, PIG_VARIANT);
    }
}
