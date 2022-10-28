package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
        return ObjectSet.of(EntityType.ENDERMAN, EntityType.BLAZE, EntityType.SNOWMAN);
    }

    public static Set<EntityType> canBreatheUnderWater()
    {
        return ObjectSet.of(EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH,
                EntityType.SQUID, EntityType.GLOW_SQUID,
                EntityType.AXOLOTL, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.DOLPHIN);
    }

    public static Set<EntityType> burnsUnderSun()
    {
        return ObjectSet.of(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
                EntityType.SKELETON, EntityType.STRAY,
                EntityType.DROWNED, EntityType.PHANTOM);
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
        //列表里一些在canFly的列表里的类型本来就不会让玩家受到摔落伤害，但还是加上比较好
        return ObjectSet.of(EntityType.IRON_GOLEM, EntityType.CAT,
                EntityType.OCELOT, EntityType.SNOWMAN, EntityType.MAGMA_CUBE,
                EntityType.BAT, EntityType.BLAZE, EntityType.ENDER_DRAGON,
                EntityType.GHAST, EntityType.PARROT, EntityType.VEX,
                EntityType.WITHER, EntityType.CHICKEN, EntityType.SHULKER);
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
}
