package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class HorseValues extends AbstractHorseValues
{
    public final SingleValue<Integer> VARIANT = getSingle(0);

    public HorseValues()
    {
        super();

        registerSingle(VARIANT);
    }
}
