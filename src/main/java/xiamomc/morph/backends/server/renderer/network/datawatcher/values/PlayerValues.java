package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = getSingle(0f);
    public final SingleValue<Integer> SCORE = getSingle(0);
    public final SingleValue<Byte> SKIN_FLAGS = getSingle((byte)0); //127
    public final SingleValue<Byte> MAINHAND = getSingle((byte)1);
    public final SingleValue<CompoundTag> LEFT_SHOULDER_PARROT_COMPOUND = getSingle(new CompoundTag());
    public final SingleValue<CompoundTag> RIGHT_SHOULDER_PARROT_COMPOUND = getSingle(new CompoundTag());

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN_FLAGS, MAINHAND, LEFT_SHOULDER_PARROT_COMPOUND, RIGHT_SHOULDER_PARROT_COMPOUND);
    }
}
