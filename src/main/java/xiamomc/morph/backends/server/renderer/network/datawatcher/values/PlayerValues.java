package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = getSingle("player_absorption_amount", 0f);
    public final SingleValue<Integer> SCORE = getSingle("player_score", 0);
    public final SingleValue<Byte> SKIN_FLAGS = getSingle("player_skin_flags", (byte)0); //127
    public final SingleValue<Byte> MAINHAND = getSingle("player_mainhand", (byte)1);
    public final SingleValue<CompoundTag> LEFT_SHOULDER_PARROT_COMPOUND = getSingle("player_lSPC", new CompoundTag());
    public final SingleValue<CompoundTag> RIGHT_SHOULDER_PARROT_COMPOUND = getSingle("player_rSPC", new CompoundTag());

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN_FLAGS, MAINHAND, LEFT_SHOULDER_PARROT_COMPOUND, RIGHT_SHOULDER_PARROT_COMPOUND);
    }
}
