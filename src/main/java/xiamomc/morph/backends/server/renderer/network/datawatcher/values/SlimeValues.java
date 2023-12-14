package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class SlimeValues extends MobValues
{
    public final SingleValue<Integer> SIZE = getSingle(1);

    public SlimeValues()
    {
        super();

        registerSingle(SIZE);
    }
}
