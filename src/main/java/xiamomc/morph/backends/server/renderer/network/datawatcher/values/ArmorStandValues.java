package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.Rotations;

public class ArmorStandValues extends LivingEntityValues
{
    public final SingleValue<Byte> DATA_FLAGS = getSingle((byte)0);
    public final SingleValue<Rotations> HEAD_ROTATION = getSingle(new Rotations(0, 0, 0));
    public final SingleValue<Rotations> BODY_ROTATION = getSingle(new Rotations(0, 0, 0));
    public final SingleValue<Rotations> LEFT_ARM_ROTATION = getSingle(new Rotations(-10, 0, -10));
    public final SingleValue<Rotations> RIGHT_ARM_ROTATION = getSingle(new Rotations(-15, 0, 10));
    public final SingleValue<Rotations> LEFT_LEG_ROTATION = getSingle(new Rotations(-1, 0, -1));
    public final SingleValue<Rotations> RIGHT_LEG_ROTATION = getSingle(new Rotations(1, 0, 1));

    public ArmorStandValues()
    {
        super();

        registerSingle(DATA_FLAGS,
                HEAD_ROTATION, BODY_ROTATION,
                LEFT_ARM_ROTATION, RIGHT_ARM_ROTATION,
                LEFT_LEG_ROTATION, RIGHT_LEG_ROTATION);
    }
}
