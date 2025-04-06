package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

import java.util.Optional;
import java.util.UUID;

public class FoxValues extends AnimalValues
{
    public final SingleValue<Integer> FOX_VARIANT = createSingle("fox_variant", 0, EntityDataTypes.INT);
    public final SingleValue<Byte> FLAGS = createSingle("fox_flags", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Optional<UUID>> TRUSTED_ID_0 = createSingle("fox_trusted_0", Optional.of(UUID.randomUUID()), EntityDataTypes.OPTIONAL_UUID);
    public final SingleValue<Optional<UUID>> TRUSTED_ID_1 = createSingle("fox_trusted_1", Optional.of(UUID.randomUUID()), EntityDataTypes.OPTIONAL_UUID);

    public FoxValues()
    {
        super();

        registerSingle(FOX_VARIANT, FLAGS);
    }
}
