package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

public class GhastValues extends MobValues
{
    public final SingleValue<Boolean> CHARGING = createSingle("ghast_charging", false);

    public GhastValues()
    {
        super();

        registerSingle(CHARGING);
    }
}
