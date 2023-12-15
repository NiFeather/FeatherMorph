package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class PandaValues extends AnimalValues
{
    public final SingleValue<Integer> BREED_TIMER = getSingle(0);
    public final SingleValue<Integer> SNEEZE_TIMER = getSingle(0);
    public final SingleValue<Integer> EAT_TIMER = getSingle(0);
    public final SingleValue<Byte> MAIN_GENE = getSingle((byte)0);
    public final SingleValue<Byte> HIDDEN_GENE = getSingle((byte)0);
    public final SingleValue<Byte> FLAGS = getSingle((byte)0);

    public PandaValues()
    {
        super();

        registerSingle(BREED_TIMER, SNEEZE_TIMER, EAT_TIMER, MAIN_GENE, HIDDEN_GENE, FLAGS);
    }
}
