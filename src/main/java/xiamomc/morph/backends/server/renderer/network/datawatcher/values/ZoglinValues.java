package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ZoglinValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle("zoglin_is_baby", false);

    public ZoglinValues()
    {
        super();

        registerSingle(IS_BABY);
    }
}
