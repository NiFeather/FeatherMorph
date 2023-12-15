package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class AbstractFishValues extends WaterAnimalValues
{
    public final SingleValue<Boolean> FROM_BUCKET = getSingle(false);

    public AbstractFishValues()
    {
        super();

        registerSingle(FROM_BUCKET);
    }
}
