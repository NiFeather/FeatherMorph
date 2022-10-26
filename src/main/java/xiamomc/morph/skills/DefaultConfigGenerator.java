package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.storage.skill.*;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class DefaultConfigGenerator
{
    private static SkillConfigurationContainer cachedContainer = null;

    /**
     * 添加一个技能配置
     *
     * @param targetList 目标列表
     * @param entityType 实体类型
     * @param cd CD时间
     * @param key 技能ID
     * @param c 针对此{@link SkillConfiguration}的操作
     * @return 技能配置
     */
    private static SkillConfiguration addSkillConfiguration(List<SkillConfiguration> targetList, EntityType entityType,
                                                            int cd, NamespacedKey key, @Nullable Consumer<SkillConfiguration> c)
    {
        var config = new SkillConfiguration(entityType, cd, key);

        if (c != null)
            c.accept(config);

        targetList.add(config);

        return config;
    }

    /**
     * 添加一个技能配置
     *
     * @param targetList 目标列表
     * @param entityType 实体类型
     * @param cd CD时间
     * @param key 技能ID
     * @return 技能配置
     */
    private static SkillConfiguration addSkillConfiguration(List<SkillConfiguration> targetList, EntityType entityType,
                                                            int cd, NamespacedKey key)
    {
        return addSkillConfiguration(targetList, entityType, cd, key, null);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList, EntityType entityType, NamespacedKey key)
    {
        var cfg = targetList.stream()
                .filter(c -> c.getIdentifier().equals(entityType.getKey().asString())).findFirst().orElse(null);

        if (cfg == null)
            cfg = addSkillConfiguration(targetList, entityType, 0, SkillType.NONE);

        cfg.addAbilityIdentifier(key);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList, Set<EntityType> entityTypes, NamespacedKey key)
    {
        entityTypes.forEach(t -> addAbilityConfiguration(targetList, t, key));
    }

    public static SkillConfigurationContainer getDefaultSkillConfiguration()
    {
        if (cachedContainer != null) return cachedContainer;

        var container = new SkillConfigurationContainer();
        var skills = container.configurations;

        //伪装物品
        addSkillConfiguration(skills, EntityType.ARMOR_STAND, 20, SkillType.INVENTORY);
        addSkillConfiguration(skills, EntityType.PLAYER, 20, SkillType.INVENTORY);

        //弹射物
        addSkillConfiguration(skills, EntityType.BLAZE, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8)));

        addSkillConfiguration(skills, EntityType.ENDER_DRAGON, 80, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80)));

        addSkillConfiguration(skills, EntityType.GHAST, 40, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35)));

        addSkillConfiguration(skills, EntityType.LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addSkillConfiguration(skills, EntityType.TRADER_LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addSkillConfiguration(skills, EntityType.SHULKER, 40, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15)));

        addSkillConfiguration(skills, EntityType.SNOWMAN, 15, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8)));

        addSkillConfiguration(skills, EntityType.WITHER, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24)));

        //药效
        addSkillConfiguration(skills, EntityType.DOLPHIN, 180, SkillType.APPLY_EFFECT, c ->
                c.setEffectConfiguration(new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9)));

        addSkillConfiguration(skills, EntityType.ELDER_GUARDIAN, 1200, SkillType.APPLY_EFFECT, c ->
                c.setEffectConfiguration(new EffectConfiguration(PotionEffectType.SLOW_DIGGING.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50)));

        //其他
        addSkillConfiguration(skills, EntityType.CREEPER, 80, SkillType.EXPLODE, c ->
                c.setExplosionConfiguration(new ExplosionConfiguration(true, 3, false)));

        addSkillConfiguration(skills, EntityType.ENDERMAN, 40, SkillType.TELEPORT, c ->
                c.setTeleportConfiguration(new TeleportConfiguration(32)));

        addSkillConfiguration(skills, EntityType.EVOKER, 100, SkillType.EVOKER);

        addAbilityConfigurations(skills);

        cachedContainer = container;
        return container;
    }

    public static void addAbilityConfigurations(List<SkillConfiguration> skills)
    {
        addAbilityConfiguration(skills, EntityTypeUtils.canFly(), AbilityType.CAN_FLY);
        addAbilityConfiguration(skills, EntityTypeUtils.hasFireResistance(), AbilityType.HAS_FIRE_RESISTANCE);
        addAbilityConfiguration(skills, EntityTypeUtils.takesDamageFromWater(), AbilityType.TAKES_DAMAGE_FROM_WATER);
        addAbilityConfiguration(skills, EntityTypeUtils.canBreatheUnderWater(), AbilityType.CAN_BREATHE_UNDER_WATER);
        addAbilityConfiguration(skills, EntityTypeUtils.burnsUnderSun(), AbilityType.BURNS_UNDER_SUN);
        addAbilityConfiguration(skills, EntityTypeUtils.alwaysNightVision(), AbilityType.ALWAYS_NIGHT_VISION);
        addAbilityConfiguration(skills, EntityTypeUtils.hasJumpBoost(), AbilityType.HAS_JUMP_BOOST);
        addAbilityConfiguration(skills, EntityTypeUtils.hasSmallJumpBoost(), AbilityType.HAS_SMALL_JUMP_BOOST);
        addAbilityConfiguration(skills, EntityTypeUtils.hasSpeedBoost(), AbilityType.HAS_SPEED_BOOST);
        addAbilityConfiguration(skills, EntityTypeUtils.noFallDamage(), AbilityType.NO_FALL_DAMAGE);
        addAbilityConfiguration(skills, EntityTypeUtils.hasFeatherFalling(), AbilityType.HAS_FEATHER_FALLING);
        addAbilityConfiguration(skills, EntityTypeUtils.reducesMagicDamage(), AbilityType.REDUCES_MAGIC_DAMAGE);
        addAbilityConfiguration(skills, EntityTypeUtils.reducesFallDamage(), AbilityType.REDUCES_FALL_DAMAGE);
        addAbilityConfiguration(skills, EntityTypeUtils.hasSnowTrail(), AbilityType.SNOWY);

    }
}
