package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseTypes;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EntityTypeUtils
{
    @Nullable
    public static EntityType fromString(String key, boolean nullWhenUnknown)
    {
        var val = fromString(key);

        return val == EntityType.UNKNOWN ? null : val;
    }

    public static EntityType fromString(String key)
    {
        if (key.startsWith(DisguiseTypes.PLAYER.getNameSpace() + ":")) return EntityType.PLAYER;

        return Arrays.stream(EntityType.values())
                .filter(t -> !t.equals(EntityType.UNKNOWN) && t.getKey().asString().equals(key))
                .findFirst().orElse(EntityType.UNKNOWN);
    }

    private static final Map<EntityType, Class<? extends Entity>> nmsClassMap = new Object2ObjectOpenHashMap<>();
    private static final Map<EntityType, SoundInfo> typeSoundMap = new Object2ObjectArrayMap<>();

    static
    {
        nmsClassMap.put(EntityType.PLAYER, Player.class);
        typeSoundMap.put(EntityType.BEE, new SoundInfo(SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 120, 1));
        typeSoundMap.put(EntityType.ENDER_DRAGON, new SoundInfo(SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.HOSTILE,100, 5));
    }

    public record SoundInfo(@Nullable SoundEvent sound, SoundSource source, int interval, float volume)
    {
    }

    @NotNull
    public static SoundInfo getSoundEvent(EntityType bukkitType)
    {
        var cache = typeSoundMap.getOrDefault(bukkitType, null);
        if (cache != null) return cache;

        var nmsType = getNmsType(bukkitType);

        var serverWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        var entity = nmsType.create(serverWorld, null, e -> e.remove(Entity.RemovalReason.DISCARDED), BlockPos.ZERO, MobSpawnType.COMMAND, false, false);

        if (entity instanceof Mob mob)
        {
            var source = mob.getSoundSource();
            var sound = mob.getAmbientSound0();
            var interval = mob.getAmbientSoundInterval();

            var rec = new SoundInfo(sound, source, interval, mob.getSoundVolume());
            typeSoundMap.put(bukkitType, rec);

            return rec;
        }

        return new SoundInfo(null, SoundSource.PLAYERS, Integer.MAX_VALUE, 1);
    }

    @Nullable
    public static net.minecraft.world.entity.EntityType<?> getNmsType(@NotNull EntityType bukkitType)
    {
        return net.minecraft.world.entity.EntityType.byString(bukkitType.key().asString())
                .orElse(null);
    }

    @Nullable
    public static Class<? extends Entity> getNmsClass(@NotNull EntityType type)
    {
        var cache = nmsClassMap.getOrDefault(type, null);
        if (cache != null) return cache;

        var nmsType = net.minecraft.world.entity.EntityType.byString(type.key().asString())
                .orElse(null);

        if (nmsType == null)
        {
            nmsClassMap.put(type, null);
            return null;
        }

        var serverWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        var entity = nmsType.create(serverWorld, null, e -> e.remove(Entity.RemovalReason.DISCARDED), BlockPos.ZERO, MobSpawnType.COMMAND, false, false);

        if (entity == null)
        {
            nmsClassMap.put(type, null);
            return null;
        }

        nmsClassMap.put(type, entity.getClass());
        return entity.getClass();
    }

    public static boolean hasBabyVariant(EntityType type)
    {
        return switch (type)
        {
            case COW, SHEEP, BEE, CAMEL, CAT, CHICKEN, DONKEY,
                    FOX, GOAT, HORSE, LLAMA, MUSHROOM_COW, MULE, TRADER_LLAMA, VILLAGER,
                    OCELOT, PANDA, PIG, POLAR_BEAR, RABBIT, SNIFFER, TURTLE, WOLF,
                    HOGLIN, ZOMBIE, ZOMBIE_VILLAGER, PIGLIN, HUSK, DROWNED, ZOMBIFIED_PIGLIN, STRIDER,
                    SKELETON_HORSE, ZOMBIE_HORSE, ZOGLIN-> true;

            default -> false;
        };
    }

    public static boolean isZombie(EntityType type)
    {
        return type == EntityType.ZOMBIE
                || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.DROWNED
                || type == EntityType.HUSK;
    }

    public static boolean isSkeleton(EntityType type)
    {
        return type == EntityType.SKELETON
                || type == EntityType.STRAY;
    }

    public static boolean isZombiesHostile(EntityType type)
    {
        return isGolem(type)
                || type == EntityType.VILLAGER
                || type == EntityType.WANDERING_TRADER
                || type == EntityType.PLAYER;
    }

    public static boolean isGolem(EntityType type)
    {
        return type == EntityType.IRON_GOLEM || type == EntityType.SNOWMAN;
    }

    public static boolean isBruteHostile(EntityType type)
    {
        return isGolem(type)
                || type == EntityType.WITHER_SKELETON
                || type == EntityType.PLAYER;
    }

    public static boolean isPiglinHostile(EntityType type)
    {
        return type == EntityType.WITHER
                || type == EntityType.WITHER_SKELETON
                || type == EntityType.PLAYER;
    }

    public static boolean isGuardianHostile(EntityType type)
    {
        return type == EntityType.PLAYER
                || type == EntityType.AXOLOTL
                || type == EntityType.SQUID
                || type == EntityType.GLOW_SQUID;
    }

    public static boolean isWitherSkeletonHostile(EntityType type)
    {
        return type == EntityType.PLAYER
                || type == EntityType.PIGLIN
                || type == EntityType.PIGLIN_BRUTE
                || isGolem(type);

        //todo: 小海龟
    }

    public static boolean isZoglinHostile(EntityType type)
    {
        return type != EntityType.CREEPER
                && type != EntityType.GHAST
                && type != EntityType.ZOGLIN;
    }

    public static boolean isWitherHostile(EntityType type)
    {
        return type == EntityType.ZOMBIE
                || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.DROWNED
                || type == EntityType.HUSK
                || type == EntityType.SKELETON
                || type == EntityType.WITHER_SKELETON
                || type == EntityType.STRAY
                || type == EntityType.SKELETON_HORSE
                || type == EntityType.ZOMBIE_HORSE
                || type == EntityType.PHANTOM
                || type == EntityType.ZOMBIFIED_PIGLIN
                || type == EntityType.WITHER
                || type == EntityType.GHAST;
    }

    public static boolean isRaiderHostile(EntityType type)
    {
        return type == EntityType.PLAYER
                || type == EntityType.VILLAGER
                || type == EntityType.WANDERING_TRADER
                || isGolem(type);
    }

    public static Set<EntityType> wardenLessAware()
    {
        return ObjectSet.of(EntityType.ARMOR_STAND, EntityType.WARDEN);
    }

    public static Set<EntityType> canFly()
    {
        return ObjectSet.of(EntityType.ALLAY, EntityType.ENDER_DRAGON,
                EntityType.BAT, EntityType.BEE, EntityType.BLAZE,
                EntityType.GHAST, EntityType.VEX, EntityType.PHANTOM, EntityType.WITHER,
                EntityType.PARROT);
    }

    public static Set<EntityType> hasFireResistance()
    {
        return ObjectSet.of(EntityType.MAGMA_CUBE, EntityType.BLAZE, EntityType.WITHER_SKELETON,
                EntityType.WITHER, EntityType.STRIDER, EntityType.ZOMBIFIED_PIGLIN, EntityType.GHAST,
                EntityType.WARDEN, EntityType.ENDER_DRAGON, EntityType.ZOGLIN);
    }

    public static Set<EntityType> takesDamageFromWater()
    {
        return ObjectSet.of(EntityType.ENDERMAN, EntityType.BLAZE, EntityType.SNOWMAN, EntityType.STRIDER);
    }

    public static Set<EntityType> canBreatheUnderWater()
    {
        return ObjectSet.of(EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH,
                EntityType.SQUID, EntityType.GLOW_SQUID,
                EntityType.AXOLOTL, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.DOLPHIN,
                EntityType.TADPOLE, EntityType.DROWNED);
    }

    public static Set<EntityType> dryOutInAir()
    {
        return ObjectSet.of(EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH,
                EntityType.SQUID, EntityType.GLOW_SQUID,
                EntityType.AXOLOTL, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.DOLPHIN,
                EntityType.TADPOLE);
    }

    public static Set<EntityType> burnsUnderSun()
    {
        return ObjectSet.of(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
                EntityType.SKELETON, EntityType.STRAY,
                EntityType.DROWNED, EntityType.PHANTOM);
    }

    public static Set<EntityType> spider()
    {
        return ObjectSet.of(EntityType.SPIDER, EntityType.CAVE_SPIDER);
    }

    public static Set<EntityType> alwaysNightVision()
    {
        return ObjectSet.of(EntityType.BAT, EntityType.ENDERMAN);
    }

    public static EntityType hasJumpBoost()
    {
        return EntityType.MAGMA_CUBE;
    }

    public static EntityType hasSmallJumpBoost()
    {
        return EntityType.RABBIT;
    }

    public static EntityType hasSpeedBoost()
    {
        return EntityType.HORSE;
    }

    public static Set<EntityType> noFallDamage()
    {
        return ObjectSet.of(EntityType.IRON_GOLEM, EntityType.CAT,
                EntityType.OCELOT, EntityType.SNOWMAN, EntityType.MAGMA_CUBE,
                EntityType.CHICKEN, EntityType.SHULKER);
    }

    public static Set<EntityType> noFallDamage1()
    {
        return ObjectSet.of(EntityType.BAT, EntityType.BLAZE, EntityType.ENDER_DRAGON,
                EntityType.GHAST, EntityType.PARROT, EntityType.VEX,
                EntityType.WITHER);
    }

    public static EntityType reducesMagicDamage()
    {
        return EntityType.WITCH;
    }

    public static EntityType reducesFallDamage()
    {
        return EntityType.GOAT;
    }

    public static EntityType hasFeatherFalling()
    {
        return EntityType.CHICKEN;
    }

    public static EntityType hasSnowTrail()
    {
        return EntityType.SNOWMAN;
    }

    public static boolean saddleable(EntityType type)
    {
        return type == EntityType.HORSE || type == EntityType.MULE || type == EntityType.DONKEY
                || type == EntityType.CAMEL || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
    }

    public static boolean hasBossBar(EntityType type)
    {
        return type == EntityType.ENDER_DRAGON || type == EntityType.WITHER;
    }

    public static float getDefaultFlyingSpeed(@Nullable EntityType type)
    {
        if (type == null) return 0.1f;

        return switch (type)
        {
            case ALLAY, BEE, BLAZE, VEX, BAT, PARROT -> 0.05f;
            case GHAST, PHANTOM -> 0.06f;
            case ENDER_DRAGON -> 0.15f;
            default -> 0.1f;
        };
    }

    private static final Map<EntityType, Boolean> isEnemyMap = new Object2ObjectArrayMap<>();

    private static final Location spawnLocation = new Location(null, 0d, -4096d, 0d);

    public static boolean isEnemy(EntityType type)
    {
        var cache = isEnemyMap.getOrDefault(type, null);
        if (cache != null) return cache;

        if (type.getEntityClass() == null) return false;

        var world = Bukkit.getWorlds().stream().findFirst().orElse(null);
        if (world == null) return false;

        var entity = world.spawn(spawnLocation, type.getEntityClass());
        var isEnemy = entity instanceof Enemy;

        ((CraftEntity) entity).getHandle().discard();

        isEnemyMap.put(type, isEnemy);
        return isEnemy;
    }
}
