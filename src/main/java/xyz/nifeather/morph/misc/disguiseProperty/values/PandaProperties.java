package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PandaProperties extends BaseLivingEntityProperties<Panda>
{
    private final Map<String, Panda.Gene> geneMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var gene : Gene.values())
            geneMap.put(gene.name().toLowerCase(), gene);
    }

    public final SingleProperty<Panda.Gene> MAIN_GENE = getSingle("panda/main_gene", Gene.NORMAL)
            .withRandom(Gene.values());

    public final SingleProperty<Panda.Gene> HIDDEN_GENE = getSingle("panda/hidden_gene", Gene.NORMAL)
            .withRandom(Gene.values());

    public PandaProperties()
    {
        initMap();

        MAIN_GENE.withValidInput(geneMap.keySet());
        HIDDEN_GENE.withValidInput(geneMap.keySet());

        registerSingle(MAIN_GENE, HIDDEN_GENE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        switch (key)
        {
            case "panda/main_gene" ->
            {
                var gene = geneMap.getOrDefault(value, null);

                if (gene != null)
                    return Pair.of(MAIN_GENE, gene);
            }

            case "panda/hidden_gene" ->
            {
                var gene = geneMap.getOrDefault(value, null);

                if (gene != null)
                    return Pair.of(HIDDEN_GENE, gene);
            }
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Panda tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Panda panda ? panda : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Panda targetEntity)
    {
        propertyHandler.set(MAIN_GENE, targetEntity.getMainGene());
        propertyHandler.set(HIDDEN_GENE, targetEntity.getHiddenGene());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(MAIN_GENE, DisguiseUtils.pick(MAIN_GENE.getRandomValues()));
        propertyHandler.set(HIDDEN_GENE, DisguiseUtils.pick(HIDDEN_GENE.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(MAIN_GENE.id(), propertyHandler.get(MAIN_GENE).name().toLowerCase());
        map.put(HIDDEN_GENE.id(), propertyHandler.get(HIDDEN_GENE).name().toLowerCase());
    }
}
