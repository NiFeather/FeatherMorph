package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.ChestedHorseValues;

public class LlamaValues extends ChestedHorseValues
{
    public final SingleValue<Integer> SLOTS = createSingle("llama_slots", 2, EntityDataTypes.INT);
    public final SingleValue<Integer> VARIANT = createSingle("llama_variant", 1, EntityDataTypes.INT);

    public LlamaValues()
    {
        super();

        registerSingle(SLOTS, VARIANT);
    }
}
