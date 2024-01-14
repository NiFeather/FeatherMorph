package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;
import java.util.UUID;

public class TameableAnimalValues extends AnimalValues
{
    public final SingleValue<Byte> TAMEABLE_FLAGS = getSingle("tameable_flags", (byte)0);
    public final SingleValue<Optional<UUID>> OWNER = getSingle("tameable_owner", Optional.of(UUID.randomUUID()));

    public TameableAnimalValues()
    {
        super();

        registerSingle(TAMEABLE_FLAGS, OWNER);
    }
}
