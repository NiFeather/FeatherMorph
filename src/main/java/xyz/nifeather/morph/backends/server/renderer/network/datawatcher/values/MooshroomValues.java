package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class MooshroomValues extends AnimalValues
{
    public final SingleValue<Integer> DATA_TYPE = createSingle("mooshroom_variant", RED);
    public static final int RED = 0;
    public static final int BROWN = 1;

    public MooshroomValues()
    {
        super();

        registerSingle(DATA_TYPE);
    }
}
