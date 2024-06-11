package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.ChestedHorseValues;

public class LlamaValues extends ChestedHorseValues
{
    public final SingleValue<Integer> SLOTS = getSingle("llama_slots", 2);
    public final SingleValue<Integer> VARIANT = getSingle("llama_variant", 1).withRandom(0, 1, 2, 3);

    public LlamaValues()
    {
        super();

        registerSingle(SLOTS, VARIANT);
    }
}
