package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class RabbitValues extends AnimalValues
{
    public final SingleValue<Integer> RABBIT_TYPE = createSingle("rabbit_type", 0);

    public RabbitValues()
    {
        super();

        registerSingle(RABBIT_TYPE);
    }
}
