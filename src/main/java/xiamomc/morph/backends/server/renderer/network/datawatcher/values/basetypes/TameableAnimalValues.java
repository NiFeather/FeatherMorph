package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.minecraft.Util;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;
import java.util.UUID;

public class TameableAnimalValues extends AnimalValues
{
    public final SingleValue<Byte> TAMEABLE_FLAGS = getSingle("tameable_flags", (byte)0x00, EntityDataTypes.BYTE);
    public final SingleValue<Optional<UUID>> OWNER = getSingle("tameable_owner", Optional.of(Util.NIL_UUID), EntityDataTypes.OPTIONAL_UUID);

    public TameableAnimalValues()
    {
        super();

        registerSingle(TAMEABLE_FLAGS, OWNER);
    }
}
