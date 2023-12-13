package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.BlockPos;
import org.bukkit.Location;

import java.util.Optional;

public class LivingEntityValues extends AbstractValues
{
    public final SingleValue<Byte> LIVING_FLAGS = SingleValue.of(8, (byte)0);
    public final SingleValue<Float> HEALTH = SingleValue.of(9, 1f);
    public final SingleValue<Integer> POTION_COLOR = SingleValue.of(10, 0);
    public final SingleValue<Boolean> POTION_ISAMBIENT = SingleValue.of(11, false);
    public final SingleValue<Integer> STUCKED_ARROWS = SingleValue.of(12, 0);
    public final SingleValue<Integer> BEE_STINGERS = SingleValue.of(13, 0);
    public final SingleValue<Optional<BlockPos>> BED_POS = SingleValue.of(14, Optional.of(new BlockPos(0,0,0)));

    public LivingEntityValues()
    {
        super();

        registerValue(LIVING_FLAGS, HEALTH, POTION_COLOR, POTION_ISAMBIENT, STUCKED_ARROWS, BED_POS, BEE_STINGERS);
    }
}
