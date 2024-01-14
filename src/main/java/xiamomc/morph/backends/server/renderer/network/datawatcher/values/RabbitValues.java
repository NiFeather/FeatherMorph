package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class RabbitValues extends AnimalValues
{
    public final SingleValue<Integer> RABBIT_TYPE = getSingle("rabbit_type", 0);

    public RabbitValues()
    {
        super();

        registerSingle(RABBIT_TYPE);
    }
}
