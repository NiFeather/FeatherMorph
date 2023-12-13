package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class AllayValues extends MobValues
{
    public final SingleValue<Boolean> DANCING = getSingle(false);
    public final SingleValue<Boolean> CAN_DUPLICATE = getSingle(false);

    public AllayValues()
    {
        super();

        this.registerSingle(DANCING);
    }
}