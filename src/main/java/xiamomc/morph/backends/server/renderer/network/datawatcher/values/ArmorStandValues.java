package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import net.minecraft.core.Rotations;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class ArmorStandValues extends LivingEntityValues
{
    public final SingleValue<Byte> DATA_FLAGS = getSingle("armor_stand_flags", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Vector3f> HEAD_ROTATION = getSingle("armor_stand_headRot", new Vector3f(0, 0, 0), EntityDataTypes.ROTATION);
    public final SingleValue<Vector3f> BODY_ROTATION = getSingle("armor_stand_bodyRot", new Vector3f(0, 0, 0), EntityDataTypes.ROTATION);
    public final SingleValue<Vector3f> LEFT_ARM_ROTATION = getSingle("armor_stand_leftArmRot", new Vector3f(-10, 0, -10), EntityDataTypes.ROTATION);
    public final SingleValue<Vector3f> RIGHT_ARM_ROTATION = getSingle("armor_stand_rightArmRot", new Vector3f(-15, 0, 10), EntityDataTypes.ROTATION);
    public final SingleValue<Vector3f> LEFT_LEG_ROTATION = getSingle("armor_stand_leftLegRot", new Vector3f(-1, 0, -1), EntityDataTypes.ROTATION);
    public final SingleValue<Vector3f> RIGHT_LEG_ROTATION = getSingle("armor_stand_rightLeg", new Vector3f(1, 0, 1), EntityDataTypes.ROTATION);

    public ArmorStandValues()
    {
        super();

        registerSingle(DATA_FLAGS,
                HEAD_ROTATION, BODY_ROTATION,
                LEFT_ARM_ROTATION, RIGHT_ARM_ROTATION,
                LEFT_LEG_ROTATION, RIGHT_LEG_ROTATION);
    }
}
