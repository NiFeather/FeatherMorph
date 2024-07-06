package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class MooshroomValues extends AnimalValues
{
    public final SingleValue<String> VARIANT = getSingle("mooshroom_variant", RED, EntityDataTypes.STRING).withRandom(RED, RED, RED, BROWN);
    public static final String RED = "red";
    public static final String BROWN = "brown";

    public MooshroomValues()
    {
        super();

        registerSingle(VARIANT);
    }
}
