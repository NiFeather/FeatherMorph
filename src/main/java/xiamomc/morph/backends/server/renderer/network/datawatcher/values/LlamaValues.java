package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class LlamaValues extends ChestedHorseValues
{
    public final SingleValue<Integer> SLOTS = getSingle(0);
    public final SingleValue<Integer> CARPET_COLOR = getSingle(-1);
    public final SingleValue<Integer> VARIANT = getSingle(0);

    public LlamaValues()
    {
        super();

        registerSingle(SLOTS, CARPET_COLOR, VARIANT);
    }
}
