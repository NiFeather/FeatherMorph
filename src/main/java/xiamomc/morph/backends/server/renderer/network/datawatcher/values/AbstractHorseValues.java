package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class AbstractHorseValues extends AgeableMobValues
{
    public final SingleValue<Byte> FLAGS = getSingle((byte)0);

    public AbstractHorseValues()
    {
        super();

        registerSingle(FLAGS);
    }
}
