package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZombieVillagerProperties extends BaseLivingEntityProperties<ZombieVillager>
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

    public final SingleProperty<Villager.Type> TYPE = createProperty(PropertyNames.ZOMBIE_VILLAGER_TYPE, Villager.Type.PLAINS, InputHandles::readVillagerType, OutputHandles::writeKeyed)
            .withRandom(Registry.VILLAGER_TYPE.stream().toList());

    public final SingleProperty<Villager.Profession> PROFESSION = createProperty(PropertyNames.ZOMBIE_VILLAGER_PROFESSION, Villager.Profession.NONE, InputHandles::readVillagerProfession, OutputHandles::writeKeyed)
            .withRandom(Registry.VILLAGER_PROFESSION.stream().toList());

    public final SingleProperty<Integer> LEVEL = createProperty(PropertyNames.ZOMBIE_VILLAGER_LEVEL, 1, InputHandles::readVillagerLevel, OutputHandles::writeInteger)
            .withRandom(1, 2, 3, 4, 5, 6);

    public final SingleProperty<Boolean> IS_BABY = createProperty(PropertyNames.ZOMBIE_VILLAGER_IS_BABY, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean);

    public ZombieVillagerProperties()
    {
        initMaps();
        TYPE.withValidInput(typeMap.keySet());
        PROFESSION.withValidInput(professionMap.keySet());

        registerSingle(TYPE, PROFESSION, IS_BABY, LEVEL);
    }

    @Override
    protected @Nullable ZombieVillager tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof ZombieVillager villager ? villager : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull ZombieVillager targetEntity)
    {
        propertyHandler.set(TYPE, targetEntity.getVillagerType());
        propertyHandler.set(PROFESSION, targetEntity.getVillagerProfession());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(TYPE, DisguiseUtils.pick(TYPE.getRandomValues()));
        propertyHandler.set(PROFESSION, DisguiseUtils.pick(PROFESSION.getRandomValues()));
    }

}
