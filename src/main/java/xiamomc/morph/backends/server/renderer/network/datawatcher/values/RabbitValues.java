package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class RabbitValues extends AnimalValues
{
    public final SingleValue<Integer> RABBIT_TYPE = getSingle(0);

    public RabbitValues()
    {
        super();

        registerSingle(RABBIT_TYPE);
    }
}
