package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

public class AllayValues extends MobValues
{
    public final SingleValue<Boolean> DANCING = createSingle("allay_dancing", false);
    public final SingleValue<Boolean> CAN_DUPLICATE = createSingle("allay_can_dupe", false);

    public AllayValues()
    {
        super();

        this.registerSingle(DANCING);
    }
}