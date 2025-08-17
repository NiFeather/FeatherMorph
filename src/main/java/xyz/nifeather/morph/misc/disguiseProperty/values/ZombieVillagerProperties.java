package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.MathUtils;

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

    public final SingleProperty<Villager.Type> TYPE = getSingle(PropertyNames.ZOMBIE_VILLAGER_TYPE, Villager.Type.PLAINS)
            .withRandom(Registry.VILLAGER_TYPE.stream().toList());

    public final SingleProperty<Villager.Profession> PROFESSION = getSingle(PropertyNames.ZOMBIE_VILLAGER_PROFESSION, Villager.Profession.NONE)
            .withRandom(Registry.VILLAGER_PROFESSION.stream().toList());

    public final SingleProperty<Integer> LEVEL = getSingle(PropertyNames.ZOMBIE_VILLAGER_LEVEL, 1)
            .withRandom(1, 2, 3, 4, 5, 6);

    public final SingleProperty<Boolean> IS_BABY = getSingle(PropertyNames.ZOMBIE_VILLAGER_IS_BABY, false);

    public ZombieVillagerProperties()
    {
        initMaps();
        TYPE.withValidInput(typeMap.keySet());
        PROFESSION.withValidInput(professionMap.keySet());

        registerSingle(TYPE, PROFESSION, IS_BABY, LEVEL);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        switch (key)
        {
            case PropertyNames.ZOMBIE_VILLAGER_TYPE ->
            {
                var type = typeMap.getOrDefault(value, null);

                if (type != null)
                    return Pair.of(TYPE, type);
            }

            case PropertyNames.ZOMBIE_VILLAGER_PROFESSION ->
            {
                var profession = professionMap.getOrDefault(value, null);

                if (profession != null)
                    return Pair.of(PROFESSION, profession);
            }

            case PropertyNames.ZOMBIE_VILLAGER_IS_BABY ->
            {
                return Pair.of(IS_BABY, Boolean.valueOf(value));
            }

            case PropertyNames.ZOMBIE_VILLAGER_LEVEL ->
            {
                return Pair.of(LEVEL, MathUtils.clamp(1, 6, MathUtils.parseIntOr(value, 1)));
            }
        }

        return super.parseSingleInput(key, value);
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

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);

        map.put(TYPE.id(), propertyHandler.get(TYPE).key().asString());
        map.put(PROFESSION.id(), propertyHandler.get(PROFESSION).key().asString());
        map.put(LEVEL.id(), propertyHandler.get(LEVEL).toString());
    }
}
