package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class TropicalFishValues extends AbstractFishValues
{
    public final SingleValue<Integer> FISH_VARIANT = getSingle(0);

    public TropicalFishValues()
    {
        super();

        registerSingle(FISH_VARIANT);
    }
}
