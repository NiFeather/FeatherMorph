package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class AvatarValues extends LivingEntityValues
{
    public final SingleValue<Byte> MAINHAND = createSingle("player_mainhand", (byte)1, EntityDataTypes.BYTE);
    public final SingleValue<Byte> SKIN_FLAGS = createSingle("player_skin_flags", (byte)0, EntityDataTypes.BYTE); //127

    public AvatarValues()
    {
        super();

        registerSingle(MAINHAND, SKIN_FLAGS);
    }
}
