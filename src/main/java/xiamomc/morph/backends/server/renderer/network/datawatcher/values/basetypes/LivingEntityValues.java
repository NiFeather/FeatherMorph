package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.util.Vector3i;
import net.minecraft.network.syncher.EntityDataSerializers;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.List;
import java.util.Optional;

public class LivingEntityValues extends EntityValues
{
    public final SingleValue<Byte> LIVING_FLAGS = getSingle("living_flags", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Float> HEALTH = getSingle("living_health", 1f, EntityDataTypes.FLOAT);
    public final SingleValue<List<Particle<?>>> POTION_COLOR = getSingle("living_option_color", List.of(), EntityDataTypes.PARTICLES);
    public final SingleValue<Boolean> POTION_ISAMBIENT = getSingle("living_potion_is_ambient", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> STUCKED_ARROWS = getSingle("living_stucked_arrows", 0, EntityDataTypes.INT);
    public final SingleValue<Integer> BEE_STINGERS = getSingle("living_bee_stingers", 0, EntityDataTypes.INT);
    public final SingleValue<Optional<Vector3i>> BED_POS = getSingle("living_bed_pos", Optional.of(new Vector3i(0,0,0)), EntityDataTypes.OPTIONAL_BLOCK_POSITION);

    public LivingEntityValues()
    {
        super();

        registerSingle(LIVING_FLAGS, HEALTH, POTION_COLOR, POTION_ISAMBIENT, STUCKED_ARROWS, BED_POS, BEE_STINGERS);
    }
}
