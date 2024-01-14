package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class AllayValues extends MobValues
{
    public final SingleValue<Boolean> DANCING = getSingle("allay_dancing", false);
    public final SingleValue<Boolean> CAN_DUPLICATE = getSingle("allay_can_dupe", false);

    public AllayValues()
    {
        super();

        this.registerSingle(DANCING);
    }
}