package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class PandaValues extends AnimalValues
{
    public final SingleValue<Integer> BREED_TIMER = getSingle("panda_breed_timer", 0);
    public final SingleValue<Integer> SNEEZE_TIMER = getSingle("panda_sneeze_timer", 0);
    public final SingleValue<Integer> EAT_TIMER = getSingle("panda_eat_timer", 0);
    public final SingleValue<Byte> MAIN_GENE = getSingle("panda_main_gene", (byte)0);
    public final SingleValue<Byte> HIDDEN_GENE = getSingle("panda_hidden_gene", (byte)0);
    public final SingleValue<Byte> PANDA_FLAGS = getSingle("panda_flags", (byte)0);

    public PandaValues()
    {
        super();

        registerSingle(BREED_TIMER, SNEEZE_TIMER, EAT_TIMER, MAIN_GENE, HIDDEN_GENE, PANDA_FLAGS);
    }
}
