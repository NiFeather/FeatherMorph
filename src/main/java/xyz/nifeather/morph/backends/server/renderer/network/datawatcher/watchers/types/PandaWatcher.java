package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PandaPropertyCollection;

import java.util.Arrays;

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
        return Arrays.stream(Panda.Gene.values()).toList().get(read(ValueIndex.PANDA.MAIN_GENE));
    }

    public Panda.Gene getHiddenGene()
    {
        return Arrays.stream(Panda.Gene.values()).toList().get(read(ValueIndex.PANDA.HIDDEN_GENE));
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.PANDA_MAIN_GENE ->
            {
                var val = (Panda.Gene) value;
                writePersistent(ValueIndex.PANDA.MAIN_GENE, (byte)val.ordinal());
            }

            case PropertyNames.PANDA_HIDDEN_GENE ->
            {
                var val = (Panda.Gene) value;
                writePersistent(ValueIndex.PANDA.HIDDEN_GENE, (byte)val.ordinal());
            }

            case PropertyNames.PANDA_SITTING ->
            {
                var sitting = (Boolean) value;
                var flag = sitting ? 0x08 : 0x00;

                this.writePersistent(ValueIndex.PANDA.PANDA_FLAGS, (byte) flag);
            }
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var mainGene = this.getMainGene();
        var hiddenGene = this.getHiddenGene();

        nbt.putString("MainGene", mainGene.toString().toLowerCase());
        nbt.putString("HiddenGene", hiddenGene.toString().toLowerCase());
    }

    private Panda.Gene getGeneFromName(String name)
    {
        var gene = Panda.Gene.values();
        var match = Arrays.stream(gene).filter(g -> g.name().equalsIgnoreCase(name))
                .findFirst().orElse(null);

        if (match == null)
        {
            logger.warn("Null Gene for name " + name + "?!");
            match = Panda.Gene.NORMAL;
        }

        return match;
    }
}
