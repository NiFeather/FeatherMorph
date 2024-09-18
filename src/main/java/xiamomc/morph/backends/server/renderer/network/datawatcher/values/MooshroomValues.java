package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class MooshroomValues extends AnimalValues
{
    public final SingleValue<String> VARIANT = createSingle("mooshroom_variant", RED).withRandom(RED, RED, RED, BROWN);
    public static final String RED = "red";
    public static final String BROWN = "brown";

    public MooshroomValues()
    {
        super();

        registerSingle(VARIANT);
    }
}
