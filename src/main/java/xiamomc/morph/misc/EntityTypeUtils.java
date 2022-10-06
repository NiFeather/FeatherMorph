package xiamomc.morph.misc;

import org.bukkit.entity.EntityType;

public class EntityTypeUtils
{
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

    public static boolean canFly(EntityType type)
    {
        return type == EntityType.ALLAY
                || type == EntityType.ENDER_DRAGON
                || type == EntityType.BAT
                || type == EntityType.BEE
                || type == EntityType.BLAZE
                || type == EntityType.GHAST
                || type == EntityType.VEX
                || type == EntityType.PHANTOM
                || type == EntityType.WITHER;
    }

    public static boolean hasFireResistance(EntityType type)
    {
        return type == EntityType.MAGMA_CUBE
                || type == EntityType.BLAZE
                || type == EntityType.WITHER_SKELETON
                || type == EntityType.WITHER
                || type == EntityType.STRIDER;
    }

    public static boolean takesDamageFromWater(EntityType type)
    {
        return type == EntityType.ENDERMAN
                || type == EntityType.BLAZE;
    }

    public static boolean canBreatheUnderWater(EntityType type)
    {
        return type == EntityType.COD
        || type == EntityType.SALMON
        || type == EntityType.PUFFERFISH
        || type == EntityType.TROPICAL_FISH
        || type == EntityType.SQUID
        || type == EntityType.GLOW_SQUID
        || type == EntityType.AXOLOTL
        || type == EntityType.GUARDIAN
        || type == EntityType.ELDER_GUARDIAN
        || type == EntityType.DOLPHIN;
    }

    public static boolean burnsUnderSun(EntityType type)
    {
        return type == EntityType.ZOMBIE
                || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.SKELETON
                || type == EntityType.STRAY
                || type == EntityType.PHANTOM
                || type == EntityType.DROWNED;
    }

    public static boolean alwaysNightVision(EntityType type)
    {
        return type == EntityType.BAT
                || type == EntityType.ENDERMAN;
    }
}
