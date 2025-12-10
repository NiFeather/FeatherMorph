package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.HumanoidArm;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class AvatarValues extends LivingEntityValues
{
    public final SingleValue<HumanoidArm> MAINHAND = createSingle("player_mainhand", HumanoidArm.RIGHT, EntityDataTypes.HUMANOID_ARM);
    public final SingleValue<Byte> SKIN_FLAGS = createSingle("player_skin_flags", (byte)0, EntityDataTypes.BYTE); //127

    public AvatarValues()
    {
        super();

        registerSingle(MAINHAND, SKIN_FLAGS);
    }
}
