package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.nbt.CompoundTag;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = createSingle("player_absorption_amount", 0f);
    public final SingleValue<Integer> SCORE = createSingle("player_score", 0);
    public final SingleValue<Byte> SKIN_FLAGS = createSingle("player_skin_flags", (byte)0); //127
    public final SingleValue<Byte> MAINHAND = createSingle("player_mainhand", (byte)1);
    public final SingleValue<CompoundTag> LEFT_SHOULDER_PARROT_COMPOUND = createSingle("player_lSPC", new CompoundTag());
    public final SingleValue<CompoundTag> RIGHT_SHOULDER_PARROT_COMPOUND = createSingle("player_rSPC", new CompoundTag());

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN_FLAGS, MAINHAND, LEFT_SHOULDER_PARROT_COMPOUND, RIGHT_SHOULDER_PARROT_COMPOUND);
    }
}
