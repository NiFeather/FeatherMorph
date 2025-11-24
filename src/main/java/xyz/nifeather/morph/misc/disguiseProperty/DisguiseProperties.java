package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.misc.disguiseProperty.values.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisguiseProperties
{
    public static final DisguiseProperties INSTANCE = new DisguiseProperties();
    private static final Logger log = LoggerFactory.getLogger(DisguiseProperties.class);

    private final Map<EntityType, PropertyCollection<?>> handlerMap = new ConcurrentHashMap<>();

    //todo: DisguiseProperties 现在自己保有一个 ID <-> SingleProperty 的表
    //      然后其他人现在直接从 DisguiseProperties 通过 ID 获取对应的 SingleProperty!

    private final Map<String, SingleProperty<?>> idPropertyMap = new ConcurrentHashMap<>();

    /**
     * Register an instance of {@link SingleProperty} to this instance
     * @return TRUE if success, otherwise FALSE
     */
    public boolean register(SingleProperty<?> property)
    {
        if (idPropertyMap.containsKey(property.identifier()))
            return false;

        idPropertyMap.put(property.identifier(), property);
        return true;
    }

    @Nullable
    public SingleProperty<?> lookup(String identifier)
    {
        return idPropertyMap.getOrDefault(identifier, null);
    }

    public Optional<SingleProperty<?>> lookupOptional(String identifier)
    {
        return Optional.ofNullable(lookup(identifier));
    }

    public List<SingleProperty<?>> lookupRange(Collection<String> ids)
    {
        List<SingleProperty<?>> list = new ObjectArrayList<>();

        ids.forEach(id ->
        {
            var property = lookup(id);
            if (property != null)
                list.add(property);
        });

        return list;
    }

    private DisguiseProperties()
    {
        register(EntityType.FROG, new FrogPropertyCollection());
        register(EntityType.CAT, new CatPropertyCollection());
        register(EntityType.AXOLOTL, new AxolotlPropertyCollection());
        register(EntityType.FOX, new FoxPropertyCollection());
        register(EntityType.GOAT, new GoatPropertyCollection());
        register(EntityType.MOOSHROOM, new MooshroomPropertyCollection());
        register(EntityType.PARROT, new ParrotPropertyCollection());
        register(EntityType.RABBIT, new RabbitPropertyCollection());
        register(EntityType.WOLF, new WolfPropertyCollection());
        register(EntityType.LLAMA, new LlamaPropertyCollection());
        register(EntityType.HORSE, new HorsePropertyCollection());
        register(EntityType.PANDA, new PandaPropertyCollection());
        register(EntityType.VILLAGER, new VillagerPropertyCollection());
        register(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerPropertyCollection());
        register(EntityType.ARMOR_STAND, new ArmorStandPropertyCollection());
        register(EntityType.CREEPER, new CreeperPropertyCollection());

        register(EntityType.PIG, new PigPropertyCollection());
        register(EntityType.COW, new CowPropertyCollection());
        register(EntityType.CHICKEN, new ChickenPropertyCollection());

        register(EntityType.ENDER_DRAGON, new EnderDragonPropertyCollection());
        register(EntityType.HAPPY_GHAST, new HappyGhastPropertyCollection());

        register(EntityType.PLAYER, new PlayerPropertyCollection());

        var slimeMagmaProperties = new SlimeMagmaPropertyCollection();
        register(EntityType.SLIME, slimeMagmaProperties);
        register(EntityType.MAGMA_CUBE, slimeMagmaProperties);

        register(EntityType.SHULKER, new ShulkerPropertyCollection());
        register(EntityType.TRADER_LLAMA, new TraderLlamaPropertyCollection());
        register(EntityType.PHANTOM, new PhantomPropertyCollection());
        register(EntityType.SHEEP, new SheepPropertyCollection());
        register(EntityType.SNOW_GOLEM, new SnowGolemPropertyCollection());
        register(EntityType.TROPICAL_FISH, new TropicalFishPropertyCollection());

        register(EntityType.HOGLIN, new HoglinPropertyCollection());
        register(EntityType.ZOGLIN, new ZoglinPropertyCollection());

        register(EntityType.ZOMBIE, new ZombiePropertyCollection());

        register(EntityType.GUARDIAN, new GuardianPropertyCollection());
        register(EntityType.ELDER_GUARDIAN, new GuardianPropertyCollection());

        register(EntityType.MANNEQUIN, new MannequinPropertyCollection());
        register(EntityType.COPPER_GOLEM, new CopperGolemPropertyCollection());
    }

    public Map<EntityType, PropertyCollection<?>> getAll()
    {
        return new Object2ObjectOpenHashMap<>(handlerMap);
    }

    public void register(EntityType type, PropertyCollection<?> properties)
    {
        if (handlerMap.containsKey(type))
            throw new IllegalArgumentException("Already contains properties setup for type " + type);

        handlerMap.put(type, properties);
        properties.getRegisteredProperties().values().forEach(this::register);
    }

    private static final PropertyCollection<?> defaultProperties = new FallbackPropertyCollection();

    public <X> X getOrThrow(Class<X> expectedClass)
    {
        var find = handlerMap.values().stream().filter(expectedClass::isInstance)
                .findFirst()
                .orElse(null);

        if (find == null)
            throw new NullDependencyException("Can't find '%s' in registered properties.".formatted(expectedClass));

        return (X) find;
    }

    @NotNull
    public <X> X getOrThrow(EntityType type, Class<X> expetedClass)
    {
        var raw = get(type);

        if (!expetedClass.isInstance(raw))
            throw new NullDependencyException("Can't get disguise properties for type '%s', expected '%s' but got '%s'".formatted(type, expetedClass, raw.getClass()));

        return (X) raw;
    }

    @NotNull
    public PropertyCollection<?> get(EntityType type)
    {
        return handlerMap.getOrDefault(type, defaultProperties);
    }
}
