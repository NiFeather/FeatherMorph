package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

import java.util.UUID;

public class FoxValues extends AnimalValues
{
    public final SingleValue<Integer> FOX_VARIANT = createSingle("fox_variant", 0).withRandom(0, 1);
    public final SingleValue<Byte> FLAGS = createSingle("fox_flags", (byte)0);
    public final SingleValue<UUID> TRUSTED_ID_0 = createSingle("fox_trusted_0", UUID.randomUUID());
    public final SingleValue<UUID> TRUSTED_ID_1 = createSingle("fox_trusted_1", UUID.randomUUID());

    public FoxValues()
    {
        super();

        registerSingle(FOX_VARIANT, FLAGS);
    }
}
