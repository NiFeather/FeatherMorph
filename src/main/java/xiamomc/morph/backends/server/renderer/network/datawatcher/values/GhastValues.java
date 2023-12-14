package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class GhastValues extends MobValues
{
    public final SingleValue<Boolean> CHARGING = getSingle(false);

    public GhastValues()
    {
        super();

        registerSingle(CHARGING);
    }
}
