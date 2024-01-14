package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class PhantomValues extends MobValues
{
    public final SingleValue<Integer> SIZE = getSingle("phantom_size", 0);

    public PhantomValues()
    {
        super();

        registerSingle(SIZE);
    }
}
