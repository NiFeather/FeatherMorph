package xiamomc.morph.backends.server.renderer.network.datawatcher;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.GoatWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.*;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.AbstractHorseWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.HorseWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.SkeletonHorseWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.ZombieHorseWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.llama.LlamaWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.llama.TraderLlamaWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma.MagmaWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma.SlimeWatcher;

import java.util.Map;
import java.util.function.Function;

public class WatcherIndex
{
    public static WatcherIndex getInstance()
    {
        if (instance == null) instance = new WatcherIndex();
        return instance;
    }

    private static WatcherIndex instance;

    public WatcherIndex()
    {
        instance = this;

        setTypeWatcher(EntityType.PLAYER, PlayerWatcher::new);
        setTypeWatcher(EntityType.ALLAY, AllayWatcher::new);
        setTypeWatcher(EntityType.ARMOR_STAND, ArmorStandWatcher::new);
        setTypeWatcher(EntityType.SLIME, SlimeWatcher::new);
        setTypeWatcher(EntityType.MAGMA_CUBE, MagmaWatcher::new);
        setTypeWatcher(EntityType.GHAST, GhastWatcher::new);

        setTypeWatcher(EntityType.HORSE, HorseWatcher::new);
        setTypeWatcher(EntityType.SKELETON_HORSE, SkeletonHorseWatcher::new);
        setTypeWatcher(EntityType.ZOMBIE_HORSE, ZombieHorseWatcher::new);
        setTypeWatcher(EntityType.DONKEY, DonkeyWatcher::new);

        setTypeWatcher(EntityType.CAMEL, p -> new AbstractHorseWatcher(p, EntityType.CAMEL));

        setTypeWatcher(EntityType.LLAMA, LlamaWatcher::new);
        setTypeWatcher(EntityType.TRADER_LLAMA, TraderLlamaWatcher::new);

        setTypeWatcher(EntityType.FOX, FoxWatcher::new);
        setTypeWatcher(EntityType.PARROT, ParrotWatcher::new);
        setTypeWatcher(EntityType.CAT, CatWatcher::new);
        setTypeWatcher(EntityType.GOAT, GoatWatcher::new);

        setTypeWatcher(EntityType.RABBIT, RabbitWatcher::new);
        setTypeWatcher(EntityType.TROPICAL_FISH, TropicalFishWatcher::new);
        setTypeWatcher(EntityType.FROG, FrogWatcher::new);
        setTypeWatcher(EntityType.PANDA, PandaWatcher::new);

        setTypeWatcher(EntityType.VILLAGER, VillagerWatcher::new);
        setTypeWatcher(EntityType.SNOWMAN, SnowGolemWatcher::new);
        setTypeWatcher(EntityType.CREEPER, CreeperWatcher::new);
        setTypeWatcher(EntityType.PIGLIN, PiglinWatcher::new);

        setTypeWatcher(EntityType.ZOMBIE, ZombieWatcher::new);
        setTypeWatcher(EntityType.ZOMBIE_VILLAGER, ZombieVillagerWatcher::new);
        setTypeWatcher(EntityType.ZOGLIN, ZoglinWatcher::new);
        setTypeWatcher(EntityType.HOGLIN, HoglinWatcher::new);
        setTypeWatcher(EntityType.GUARDIAN, GuardianWatcher::new);

        setTypeWatcher(EntityType.AXOLOTL, AxolotlWatcher::new);

        setTypeWatcher(EntityType.MUSHROOM_COW, MooshroomWatcher::new);
        setTypeWatcher(EntityType.SHEEP, SheepWatcher::new);

        setTypeWatcher(EntityType.WOLF, WolfWatcher::new);
        setTypeWatcher(EntityType.PHANTOM, PhantomWatcher::new);
    }

    private void setTypeWatcher(EntityType type, Function<Player, SingleWatcher> func)
    {
        typeWatcherMap.put(type, func);
    }

    private final Map<EntityType, Function<Player, SingleWatcher>> typeWatcherMap = new Object2ObjectOpenHashMap<>();

    public SingleWatcher getWatcherForType(Player bindingPlayer, EntityType entityType)
    {
        var watcherFunc = typeWatcherMap.getOrDefault(entityType, null);

        if (watcherFunc == null)
            return new LivingEntityWatcher(bindingPlayer, entityType);

        return watcherFunc.apply(bindingPlayer);
    }
}
