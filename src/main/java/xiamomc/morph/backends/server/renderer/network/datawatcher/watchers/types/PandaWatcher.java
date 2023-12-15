package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.PandaValues;

import java.util.Arrays;
import java.util.Random;

public class PandaWatcher extends LivingEntityWatcher
{
    public PandaWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PANDA);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PANDA);
    }

    public Panda.Gene getMainGene()
    {
        return Arrays.stream(Panda.Gene.values()).toList().get(get(ValueIndex.PANDA.MAIN_GENE));
    }

    public Panda.Gene getHiddenGene()
    {
        return Arrays.stream(Panda.Gene.values()).toList().get(get(ValueIndex.PANDA.HIDDEN_GENE));
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var availableValues = Arrays.stream(Panda.Gene.values()).toList();
        var random = new Random();
        var mainGene = availableValues.get(random.nextInt(availableValues.size()));
        var hiddenGene = availableValues.get(random.nextInt(availableValues.size()));

        write(ValueIndex.PANDA.MAIN_GENE, (byte)mainGene.ordinal());
        write(ValueIndex.PANDA.HIDDEN_GENE, (byte)hiddenGene.ordinal());
    }
}
