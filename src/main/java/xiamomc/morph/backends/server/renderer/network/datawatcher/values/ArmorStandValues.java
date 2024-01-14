package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.Rotations;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class ArmorStandValues extends LivingEntityValues
{
    public final SingleValue<Byte> DATA_FLAGS = getSingle("armor_stand_flags", (byte)0);
    public final SingleValue<Rotations> HEAD_ROTATION = getSingle("armor_stand_headRot", new Rotations(0, 0, 0));
    public final SingleValue<Rotations> BODY_ROTATION = getSingle("armor_stand_bodyRot", new Rotations(0, 0, 0));
    public final SingleValue<Rotations> LEFT_ARM_ROTATION = getSingle("armor_stand_leftArmRot", new Rotations(-10, 0, -10));
    public final SingleValue<Rotations> RIGHT_ARM_ROTATION = getSingle("armor_stand_rightArmRot", new Rotations(-15, 0, 10));
    public final SingleValue<Rotations> LEFT_LEG_ROTATION = getSingle("armor_stand_leftLegRot", new Rotations(-1, 0, -1));
    public final SingleValue<Rotations> RIGHT_LEG_ROTATION = getSingle("armor_stand_rightLeg", new Rotations(1, 0, 1));

    public ArmorStandValues()
    {
        super();

        registerSingle(DATA_FLAGS,
                HEAD_ROTATION, BODY_ROTATION,
                LEFT_ARM_ROTATION, RIGHT_ARM_ROTATION,
                LEFT_LEG_ROTATION, RIGHT_LEG_ROTATION);
    }
}
