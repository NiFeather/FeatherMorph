package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class PlayerValues extends AvatarValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = createSingle("player_absorption_amount", 0f, EntityDataTypes.FLOAT);
    public final SingleValue<Integer> SCORE = createSingle("player_score", 0, EntityDataTypes.INT);
    public final SingleValue<NBTCompound> LEFT_SHOULDER_PARROT_COMPOUND = createSingle("player_lSPC", new NBTCompound(), EntityDataTypes.NBT);
    public final SingleValue<NBTCompound> RIGHT_SHOULDER_PARROT_COMPOUND = createSingle("player_rSPC", new NBTCompound(), EntityDataTypes.NBT);

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, LEFT_SHOULDER_PARROT_COMPOUND, RIGHT_SHOULDER_PARROT_COMPOUND);
    }
}
