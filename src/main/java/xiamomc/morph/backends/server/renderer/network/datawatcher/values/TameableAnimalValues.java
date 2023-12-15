package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import java.util.UUID;

public class TameableAnimalValues extends AnimalValues
{
    public final SingleValue<Byte> TAMEABLE_FLAGS = getSingle((byte)0);
    public final SingleValue<UUID> OWNER = getSingle(UUID.randomUUID());

    public TameableAnimalValues()
    {
        super();

        registerSingle(TAMEABLE_FLAGS, OWNER);
    }
}
