package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ChestedHorseValues extends AbstractHorseValues
{
    public final SingleValue<Boolean> HAS_CHEST = getSingle(false);

    public ChestedHorseValues()
    {
        super();

        registerSingle(HAS_CHEST);
    }
}
