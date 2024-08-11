package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.ChestedHorseValues;

public class LlamaValues extends ChestedHorseValues
{
    public final SingleValue<Integer> SLOTS = getSingle("llama_slots", 2, EntityDataTypes.INT);
    public final SingleValue<Integer> VARIANT = getSingle("llama_variant", 1, EntityDataTypes.INT).withRandom(0, 1, 2, 3);

    public LlamaValues()
    {
        super();

        registerSingle(SLOTS, VARIANT);
    }
}
