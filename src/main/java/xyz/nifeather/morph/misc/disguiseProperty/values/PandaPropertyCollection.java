package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PandaPropertyCollection extends BaseLivingEntityPropertyCollection<Panda>
{
    private final Map<String, Panda.Gene> geneMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var gene : Gene.values())
            geneMap.put(gene.name().toLowerCase(), gene);
    }

    public final SingleProperty<Panda.Gene> MAIN_GENE;

    private Optional<Gene> readGene(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Gene.values(), propertyName, string);
    }

    public final SingleProperty<Panda.Gene> HIDDEN_GENE;

    @ApiStatus.Experimental
    public final SingleProperty<Boolean> SITTING = SingleProperty.builder(PropertyNames.PANDA_SITTING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public PandaPropertyCollection()
    {
        initMap();

        MAIN_GENE = SingleProperty.builder(PropertyNames.PANDA_MAIN_GENE, Gene.NORMAL)
                .withInputHandle(this::readGene)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Gene.values())
                .withSuggestions(geneMap.keySet())
                .build();

        HIDDEN_GENE = SingleProperty.builder(PropertyNames.PANDA_HIDDEN_GENE, Gene.NORMAL)
                .withInputHandle(this::readGene)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Gene.values())
                .withSuggestions(geneMap.keySet())
                .build();

        registerSingle(MAIN_GENE, HIDDEN_GENE, SITTING);
    }

    @Override
    protected @Nullable Panda tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Panda panda ? panda : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Panda targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        propertyHandler.set(MAIN_GENE, targetEntity.getMainGene());
        propertyHandler.set(HIDDEN_GENE, targetEntity.getHiddenGene());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(MAIN_GENE, DisguiseUtils.pick(MAIN_GENE.randomValues()));
        propertyHandler.set(HIDDEN_GENE, DisguiseUtils.pick(HIDDEN_GENE.randomValues()));
    }

}
