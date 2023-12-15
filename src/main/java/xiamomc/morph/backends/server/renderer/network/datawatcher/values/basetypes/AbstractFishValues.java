package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractFishValues extends WaterAnimalValues
{
    public final SingleValue<Boolean> FROM_BUCKET = getSingle(false);

    public AbstractFishValues()
    {
        super();

        registerSingle(FROM_BUCKET);
    }
}
