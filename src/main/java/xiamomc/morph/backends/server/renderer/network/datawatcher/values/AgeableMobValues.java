package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class AgeableMobValues extends MobValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle(false);

    public AgeableMobValues()
    {
        super();

        registerSingle(IS_BABY);
    }
}
