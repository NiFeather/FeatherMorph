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

    public void loadBuiltin()
    {
        defaultProperties = new FallbackPropertyCollection();

        registerCollection(EntityType.FROG, new FrogPropertyCollection());
        registerCollection(EntityType.CAT, new CatPropertyCollection());
        registerCollection(EntityType.AXOLOTL, new AxolotlPropertyCollection());
        registerCollection(EntityType.FOX, new FoxPropertyCollection());
        registerCollection(EntityType.GOAT, new GoatPropertyCollection());
        registerCollection(EntityType.MOOSHROOM, new MooshroomPropertyCollection());
        registerCollection(EntityType.PARROT, new ParrotPropertyCollection());
        registerCollection(EntityType.RABBIT, new RabbitPropertyCollection());
        registerCollection(EntityType.WOLF, new WolfPropertyCollection());
        registerCollection(EntityType.LLAMA, new LlamaPropertyCollection());
        registerCollection(EntityType.HORSE, new HorsePropertyCollection());
        registerCollection(EntityType.PANDA, new PandaPropertyCollection());
        registerCollection(EntityType.VILLAGER, new VillagerPropertyCollection());
        registerCollection(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerPropertyCollection());
        registerCollection(EntityType.ARMOR_STAND, new ArmorStandPropertyCollection());
        registerCollection(EntityType.CREEPER, new CreeperPropertyCollection());

        registerCollection(EntityType.PIG, new PigPropertyCollection());
        registerCollection(EntityType.COW, new CowPropertyCollection());
        registerCollection(EntityType.CHICKEN, new ChickenPropertyCollection());

        registerCollection(EntityType.ENDER_DRAGON, new EnderDragonPropertyCollection());
        registerCollection(EntityType.HAPPY_GHAST, new HappyGhastPropertyCollection());

        registerCollection(EntityType.PLAYER, new PlayerPropertyCollection());

        var slimeMagmaProperties = new SlimeMagmaPropertyCollection();
        registerCollection(EntityType.SLIME, slimeMagmaProperties);
        registerCollection(EntityType.MAGMA_CUBE, slimeMagmaProperties);

        registerCollection(EntityType.SHULKER, new ShulkerPropertyCollection());
        registerCollection(EntityType.TRADER_LLAMA, new TraderLlamaPropertyCollection());
        registerCollection(EntityType.PHANTOM, new PhantomPropertyCollection());
        registerCollection(EntityType.SHEEP, new SheepPropertyCollection());
        registerCollection(EntityType.SNOW_GOLEM, new SnowGolemPropertyCollection());
        registerCollection(EntityType.TROPICAL_FISH, new TropicalFishPropertyCollection());

        registerCollection(EntityType.HOGLIN, new HoglinPropertyCollection());
        registerCollection(EntityType.ZOGLIN, new ZoglinPropertyCollection());

        registerCollection(EntityType.ZOMBIE, new ZombiePropertyCollection());

        registerCollection(EntityType.GUARDIAN, new GuardianPropertyCollection());
        registerCollection(EntityType.ELDER_GUARDIAN, new GuardianPropertyCollection());

        registerCollection(EntityType.MANNEQUIN, new MannequinPropertyCollection());
        registerCollection(EntityType.COPPER_GOLEM, new CopperGolemPropertyCollection());

        registerCollection(EntityType.NAUTILUS, new NautilusPropertyCollection());
        registerCollection(EntityType.ZOMBIE_NAUTILUS, new ZombieNautilusPropertyCollection());
    }

    public Map<EntityType, PropertyCollection<?>> getAllCollections()
    {
        return new Object2ObjectOpenHashMap<>(handlerMap);
    }

    public List<SingleProperty<?>> getAllProperties()
    {
        return this.idPropertyMap.values().stream().toList();
    }

    public void registerCollection(EntityType type, PropertyCollection<?> properties)
    {
        if (handlerMap.containsKey(type))
            throw new IllegalArgumentException("Already contains properties setup for type " + type);

        handlerMap.put(type, properties);
        properties.getRegisteredProperties().values().forEach(this::register);
    }

    private volatile PropertyCollection<?> defaultProperties;

    public <X> X getCollectionOrThrow(Class<X> expectedClass)
    {
        var find = handlerMap.values().stream().filter(expectedClass::isInstance)
                .findFirst()
                .orElse(null);

        if (find == null)
            throw new NullDependencyException("Can't find '%s' in registered properties.".formatted(expectedClass));

        return (X) find;
    }

    @NotNull
    public <X> X getCollectionOrThrow(EntityType type, Class<X> expetedClass)
    {
        var raw = getCollection(type);

        if (!expetedClass.isInstance(raw))
            throw new NullDependencyException("Can't get disguise properties for type '%s', expected '%s' but got '%s'".formatted(type, expetedClass, raw.getClass()));

        return (X) raw;
    }

    @NotNull
    public PropertyCollection<?> getCollection(EntityType type)
    {
        return handlerMap.getOrDefault(type, Objects.requireNonNull(this.defaultProperties, "Bad implementation! Please report this bug to authors on our GitHub!"));
    }
}
