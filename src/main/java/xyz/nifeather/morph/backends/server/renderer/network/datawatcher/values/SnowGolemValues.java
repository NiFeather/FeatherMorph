package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractGolemValues;

public class SnowGolemValues extends AbstractGolemValues
{
    public final SingleValue<Byte> HAT_FLAGS = createSingle("snowman_has_pumpkin", HAS_PUMPKIN); //.withRandom(HAS_PUMPKIN, NO_PUMPKIN);

    public static byte NO_PUMPKIN = 0;
    public static byte HAS_PUMPKIN = 16;

    public SnowGolemValues()
    {
        super();

        registerSingle(HAT_FLAGS);
    }
}
