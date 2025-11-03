package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PandaProperties extends BaseLivingEntityProperties<Panda>
{
    private final Map<String, Panda.Gene> geneMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var gene : Gene.values())
            geneMap.put(gene.name().toLowerCase(), gene);
    }

    public final SingleProperty<Panda.Gene> MAIN_GENE = createProperty(PropertyNames.PANDA_MAIN_GENE, Gene.NORMAL, this::readGene, OutputHandles::writeEnum)
            .withRandom(Gene.values());

    private Optional<Gene> readGene(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Gene.values(), propertyName, string);
    }

    public final SingleProperty<Panda.Gene> HIDDEN_GENE = createProperty(PropertyNames.PANDA_HIDDEN_GENE, Gene.NORMAL, this::readGene, OutputHandles::writeEnum)
            .withRandom(Gene.values());

    public PandaProperties()
    {
        initMap();

        MAIN_GENE.withValidInput(geneMap.keySet());
        HIDDEN_GENE.withValidInput(geneMap.keySet());

        registerSingle(MAIN_GENE, HIDDEN_GENE);
    }

    @Override
    protected @Nullable Panda tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Panda panda ? panda : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Panda targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(MAIN_GENE, targetEntity.getMainGene());
        propertyHandler.set(HIDDEN_GENE, targetEntity.getHiddenGene());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(MAIN_GENE, DisguiseUtils.pick(MAIN_GENE.getRandomValues()));
        propertyHandler.set(HIDDEN_GENE, DisguiseUtils.pick(HIDDEN_GENE.getRandomValues()));
    }

}
