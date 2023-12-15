package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import java.util.UUID;

public class FoxValues extends AnimalValues
{
    public final SingleValue<Integer> VARIANT = getSingle(0).withRandom(0, 1);
    public final SingleValue<Byte> FLAGS = getSingle((byte)0);
    public final SingleValue<UUID> TRUSTED_ID_0 = getSingle(UUID.randomUUID());
    public final SingleValue<UUID> TRUSTED_ID_1 = getSingle(UUID.randomUUID());

    public FoxValues()
    {
        super();

        registerSingle(VARIANT, FLAGS);
    }
}
