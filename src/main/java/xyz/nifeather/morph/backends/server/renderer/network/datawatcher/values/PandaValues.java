package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class PandaValues extends AnimalValues
{
    public final SingleValue<Integer> BREED_TIMER = createSingle("panda_breed_timer", 0);
    public final SingleValue<Integer> SNEEZE_TIMER = createSingle("panda_sneeze_timer", 0);
    public final SingleValue<Integer> EAT_TIMER = createSingle("panda_eat_timer", 0);
    public final SingleValue<Byte> MAIN_GENE = createSingle("panda_main_gene", (byte)0);
    public final SingleValue<Byte> HIDDEN_GENE = createSingle("panda_hidden_gene", (byte)0);
    public final SingleValue<Byte> PANDA_FLAGS = createSingle("panda_flags", (byte)0);

    public PandaValues()
    {
        super();

        registerSingle(BREED_TIMER, SNEEZE_TIMER, EAT_TIMER, MAIN_GENE, HIDDEN_GENE, PANDA_FLAGS);
    }
}
