package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class PandaProperties extends AbstractProperties
{
    public final SingleProperty<Panda.Gene> MAIN_GENE = getSingle("panda_main_gene", Gene.NORMAL)
            .withRandom(Gene.values());

    public final SingleProperty<Panda.Gene> HIDDEN_GENE = getSingle("panda_hidden_gene", Gene.NORMAL)
            .withRandom(Gene.values());

    public PandaProperties()
    {
        registerSingle(MAIN_GENE, HIDDEN_GENE);
    }
}
