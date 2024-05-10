package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataSerializers;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.List;
import java.util.Optional;

public class LivingEntityValues extends EntityValues
{
    public final SingleValue<Byte> LIVING_FLAGS = getSingle("living_flags", (byte)0);
    public final SingleValue<Float> HEALTH = getSingle("living_health", 1f);
    public final SingleValue<List<ParticleOptions>> POTION_COLOR = getSingle("living_option_color", List.of());
    public final SingleValue<Boolean> POTION_ISAMBIENT = getSingle("living_potion_is_ambient", false);
    public final SingleValue<Integer> STUCKED_ARROWS = getSingle("living_stucked_arrows", 0);
    public final SingleValue<Integer> BEE_STINGERS = getSingle("living_bee_stingers", 0);
    public final SingleValue<Optional<BlockPos>> BED_POS = getSingle("living_bed_pos", Optional.of(new BlockPos(0,0,0)));

    public LivingEntityValues()
    {
        super();

        var handle = WrappedDataWatcher.Registry.fromHandle(EntityDataSerializers.PARTICLES);
        POTION_COLOR.setSerializer(handle);

        registerSingle(LIVING_FLAGS, HEALTH, POTION_COLOR, POTION_ISAMBIENT, STUCKED_ARROWS, BED_POS, BEE_STINGERS);
    }
}
