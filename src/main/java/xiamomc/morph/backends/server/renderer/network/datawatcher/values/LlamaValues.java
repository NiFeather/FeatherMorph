package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.ChestedHorseValues;

public class LlamaValues extends ChestedHorseValues
{
    public final SingleValue<Integer> SLOTS = getSingle("llama_slots", 0);
    public final SingleValue<Integer> CARPET_COLOR = getSingle("llama_carpet_color", -1);
    public final SingleValue<Integer> VARIANT = getSingle("llama_variant", 0);

    public LlamaValues()
    {
        super();

        registerSingle(SLOTS, CARPET_COLOR, VARIANT);
    }
}
