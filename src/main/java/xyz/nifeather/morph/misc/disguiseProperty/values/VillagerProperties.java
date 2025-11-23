package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VillagerProperties extends BaseLivingEntityProperties<Villager>
{
    private final Map<String, Villager.Type> typeMap = new ConcurrentHashMap<>();
    private final Map<String, Villager.Profession> professionMap = new ConcurrentHashMap<>();

    private void initMaps()
    {
        for (var type : Registry.VILLAGER_TYPE)
            typeMap.put(type.key().asString(), type);

        for (var profession : Registry.VILLAGER_PROFESSION)
            professionMap.put(profession.key().asString(), profession);
    }

    public final SingleProperty<Villager.Type> TYPE;
    public final SingleProperty<Villager.Profession> PROFESSION;
    public final SingleProperty<Integer> LEVEL;

    public VillagerProperties()
    {
        initMaps();

        TYPE = SingleProperty.builder(PropertyNames.VILLAGER_TYPE, Villager.Type.PLAINS)
                .withInputHandle(InputHandles::readVillagerType)
                .withOutputHandle(OutputHandles::writeKeyed)
                .withRandom(Registry.VILLAGER_TYPE.stream().toList())
                .withValidInput(typeMap.keySet())
                .build();

        PROFESSION = SingleProperty.builder(PropertyNames.VILLAGER_PROFESSION, Villager.Profession.NONE)
                .withInputHandle(InputHandles::readVillagerProfession)
                .withOutputHandle(OutputHandles::writeKeyed)
                .withRandom(Registry.VILLAGER_PROFESSION.stream().toList())
                .withValidInput(professionMap.keySet())
                .build();

        LEVEL = SingleProperty.builder(PropertyNames.VILLAGER_LEVEL, 1)
                .withInputHandle(InputHandles::readInteger)
                .withOutputHandle(OutputHandles::writeInteger)
                .withRandom(1, 2, 3, 4, 5, 6)
                .withValidInput("1", "2", "3", "4", "5", "6")
                .build();

        registerSingle(TYPE, PROFESSION, LEVEL);
    }

    @Override
    protected @Nullable Villager tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Villager villager ? villager : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Villager targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(TYPE, targetEntity.getVillagerType());
        propertyHandler.set(PROFESSION, targetEntity.getProfession());
        propertyHandler.set(LEVEL, targetEntity.getVillagerLevel());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(TYPE, DisguiseUtils.pick(TYPE.randomValues()));
        propertyHandler.set(PROFESSION, DisguiseUtils.pick(PROFESSION.randomValues()));
        propertyHandler.set(LEVEL, DisguiseUtils.pick(LEVEL.randomValues()));
    }

}
