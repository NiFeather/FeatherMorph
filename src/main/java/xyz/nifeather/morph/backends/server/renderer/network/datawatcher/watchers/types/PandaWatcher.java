package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PandaProperties;

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
        var properties = DisguiseProperties.INSTANCE.getOrThrow(PandaProperties.class);

        if (property.equals(properties.MAIN_GENE))
        {
            var val = (Panda.Gene) value;
            writePersistent(ValueIndex.PANDA.MAIN_GENE, (byte)val.ordinal());
        }

        if (property.equals(properties.HIDDEN_GENE))
        {
            var val = (Panda.Gene) value;
            writePersistent(ValueIndex.PANDA.HIDDEN_GENE, (byte)val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.EAT -> this.writePersistent(ValueIndex.PANDA.EAT_TIMER, 100);
                case AnimationNames.SIT -> this.writePersistent(ValueIndex.PANDA.PANDA_FLAGS, (byte)0x08);
                case AnimationNames.STANDUP, AnimationNames.RESET -> this.writePersistent(ValueIndex.PANDA.PANDA_FLAGS, (byte)0x00);
            }
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("MainGene"))
            writePersistent(ValueIndex.PANDA.MAIN_GENE, (byte)getGeneFromName(nbt.getString("MainGene")).ordinal());

        if (nbt.contains("HiddenGene"))
            writePersistent(ValueIndex.PANDA.HIDDEN_GENE, (byte)getGeneFromName(nbt.getString("HiddenGene")).ordinal());
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
