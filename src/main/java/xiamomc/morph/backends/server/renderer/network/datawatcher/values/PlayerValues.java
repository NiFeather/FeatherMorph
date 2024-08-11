package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = getSingle("player_absorption_amount", 0f, EntityDataTypes.FLOAT);
    public final SingleValue<Integer> SCORE = getSingle("player_score", 0, EntityDataTypes.INT);
    public final SingleValue<Byte> SKIN_FLAGS = getSingle("player_skin_flags", (byte)0, EntityDataTypes.BYTE); //127
    public final SingleValue<Byte> MAINHAND = getSingle("player_mainhand", (byte)1, EntityDataTypes.BYTE);
    public final SingleValue<NBTCompound> LEFT_SHOULDER_PARROT_COMPOUND = getSingle("player_lSPC", new NBTCompound(), EntityDataTypes.NBT);
    public final SingleValue<NBTCompound> RIGHT_SHOULDER_PARROT_COMPOUND = getSingle("player_rSPC", new NBTCompound(), EntityDataTypes.NBT);

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN_FLAGS, MAINHAND, LEFT_SHOULDER_PARROT_COMPOUND, RIGHT_SHOULDER_PARROT_COMPOUND);
    }
}
