package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import net.minecraft.core.BlockPos;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;

public class LivingEntityValues extends EntityValues
{
    public final SingleValue<Byte> LIVING_FLAGS = getSingle((byte)0);
    public final SingleValue<Float> HEALTH = getSingle(1f);
    public final SingleValue<Integer> POTION_COLOR = getSingle(0);
    public final SingleValue<Boolean> POTION_ISAMBIENT = getSingle(false);
    public final SingleValue<Integer> STUCKED_ARROWS = getSingle(0);
    public final SingleValue<Integer> BEE_STINGERS = getSingle(0);
    public final SingleValue<Optional<BlockPos>> BED_POS = getSingle(Optional.of(new BlockPos(0,0,0)));

    public LivingEntityValues()
    {
        super();

        registerSingle(LIVING_FLAGS, HEALTH, POTION_COLOR, POTION_ISAMBIENT, STUCKED_ARROWS, BED_POS, BEE_STINGERS);
    }
}
