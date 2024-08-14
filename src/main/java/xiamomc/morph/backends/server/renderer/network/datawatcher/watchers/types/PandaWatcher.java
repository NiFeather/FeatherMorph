package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.PandaProperties;

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
        return Arrays.stream(Panda.Gene.values()).toList().get(get(ValueIndex.PANDA.MAIN_GENE));
    }

    public Panda.Gene getHiddenGene()
    {
        return Arrays.stream(Panda.Gene.values()).toList().get(get(ValueIndex.PANDA.HIDDEN_GENE));
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(PandaProperties.class);

        if (property.equals(properties.MAIN_GENE))
        {
            var val = (Panda.Gene) value;
            writeOverride(ValueIndex.PANDA.MAIN_GENE, (byte)val.ordinal());
        }

        if (property.equals(properties.HIDDEN_GENE))
        {
            var val = (Panda.Gene) value;
            writeOverride(ValueIndex.PANDA.HIDDEN_GENE, (byte)val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.EAT -> this.writeOverride(ValueIndex.PANDA.EAT_TIMER, 100);
                case AnimationNames.SIT -> this.writeOverride(ValueIndex.PANDA.PANDA_FLAGS, (byte)0x08);
                case AnimationNames.STANDUP, AnimationNames.RESET -> this.writeOverride(ValueIndex.PANDA.PANDA_FLAGS, (byte)0x00);
            }
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("MainGene"))
            writeOverride(ValueIndex.PANDA.MAIN_GENE, (byte)getGeneFromName(nbt.getString("MainGene")).ordinal());

        if (nbt.contains("HiddenGene"))
            writeOverride(ValueIndex.PANDA.HIDDEN_GENE, (byte)getGeneFromName(nbt.getString("HiddenGene")).ordinal());
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
