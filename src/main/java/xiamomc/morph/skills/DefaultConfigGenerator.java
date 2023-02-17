package xiamomc.morph.skills;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.options.AttributeModifyOption;
import xiamomc.morph.abilities.options.BossbarOption;
import xiamomc.morph.abilities.options.FlyOption;
import xiamomc.morph.abilities.options.ReduceDamageOption;
import xiamomc.morph.skills.options.EffectConfiguration;
import xiamomc.morph.skills.options.ExplosionConfiguration;
import xiamomc.morph.skills.options.ProjectiveConfiguration;
import xiamomc.morph.skills.options.TeleportConfiguration;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.skills.impl.SonicBoomMorphSkill;
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
     * @param mobId 实体ID
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     * @param c 针对此{@link SkillConfiguration}的操作
     * @return 技能配置
     */
    private static SkillConfiguration addSkillConfiguration(List<SkillConfiguration> targetList, String mobId,
                                                            int cd, NamespacedKey skillIdentifier, @Nullable Consumer<SkillConfiguration> c)
    {
        var cfg = targetList.stream()
                .filter(configuration -> configuration.getIdentifier().equals(mobId))
                .findFirst().orElse(null);

        SkillConfiguration config;

        if (cfg == null) config = new SkillConfiguration(mobId, cd, skillIdentifier);
        else config = cfg;

        if (c != null)
            c.accept(config);

        if (cfg == null)
            targetList.add(config);

        return config;
    }

    private static SkillConfiguration addSkillConfiguration(List<SkillConfiguration> targetList, EntityType entityType,
                                                            int cd, NamespacedKey skillIdentifier, @Nullable Consumer<SkillConfiguration> c)
    {
        return addSkillConfiguration(targetList, entityType.getKey().asString(), cd, skillIdentifier, c);
    }

    /**
     * 添加一个技能配置
     *
     * @param targetList 目标列表
     * @param entityType 实体类型
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     * @return 技能配置
     */
    private static SkillConfiguration addSkillConfiguration(List<SkillConfiguration> targetList, EntityType entityType,
                                                            int cd, NamespacedKey skillIdentifier)
    {
        return addSkillConfiguration(targetList, entityType, cd, skillIdentifier, null);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList,
                                                String mobId, NamespacedKey key,
                                                @Nullable Consumer<SkillConfiguration> consumer)
    {
        var cfg = targetList.stream()
                .filter(c -> c.getIdentifier().equals(mobId)).findFirst().orElse(null);

        if (cfg == null)
            cfg = addSkillConfiguration(targetList, mobId, 0, SkillType.NONE, null);

        if (consumer != null)
            consumer.accept(cfg);

        cfg.addAbilityIdentifier(key);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList,
                                                EntityType entityType, NamespacedKey key,
                                                @Nullable Consumer<SkillConfiguration> consumer)
    {
        addAbilityConfiguration(targetList, entityType.getKey().asString(), key, consumer);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList,
                                                EntityType entityType, NamespacedKey key)
    {
        addAbilityConfiguration(targetList, entityType, key, null);
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList,
                                                Set<EntityType> entityTypes, NamespacedKey key,
                                                @Nullable Consumer<SkillConfiguration> consumer)
    {
        entityTypes.forEach(t -> addAbilityConfiguration(targetList, t, key, consumer));
    }

    private static void addAbilityConfiguration(List<SkillConfiguration> targetList,
                                                Set<EntityType> entityTypes, NamespacedKey key)
    {
        addAbilityConfiguration(targetList, entityTypes, key, null);
    }

    public static SkillConfigurationContainer getDefaultSkillConfiguration()
    {
        if (cachedContainer != null) return cachedContainer;

        var container = new SkillConfigurationContainer();
        var skills = container.configurations;

        addSkillConfigurations(skills);
        addAbilityConfigurations(skills);

        cachedContainer = container;
        return container;
    }

    public static void addSkillConfigurations(List<SkillConfiguration> skills)
    {
        //伪装物品
        addSkillConfiguration(skills, EntityType.ARMOR_STAND, 20, SkillType.INVENTORY);
        addSkillConfiguration(skills, "player:" + MorphManager.disguiseFallbackName, 20, SkillType.INVENTORY, null);

        //弹射物
        addSkillConfiguration(skills, EntityType.BLAZE, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8)));

        addSkillConfiguration(skills, EntityType.ENDER_DRAGON, 80, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80)));

        addSkillConfiguration(skills, EntityType.LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addSkillConfiguration(skills, EntityType.TRADER_LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addSkillConfiguration(skills, EntityType.SHULKER, 40, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15)));

        addSkillConfiguration(skills, EntityType.SNOWMAN, 15, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8)));

        addSkillConfiguration(skills, EntityType.WITHER, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24)));

        addSkillConfiguration(skills, EntityType.GHAST, DisguiseUtils.GHAST_EXECUTE_DELAY + 40, SkillType.GHAST, c ->
                c.addOption(SkillType.LAUNCH_PROJECTIVE, new ProjectiveConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35)
                        .withDelay(DisguiseUtils.GHAST_EXECUTE_DELAY)
                        .withWarningSound("entity.ghast.warn")));

        //药效
        addSkillConfiguration(skills, EntityType.DOLPHIN, 180, SkillType.APPLY_EFFECT, c ->
                c.addOption(SkillType.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9)));

        addSkillConfiguration(skills, EntityType.ELDER_GUARDIAN, 1200, SkillType.APPLY_EFFECT, c ->
                c.addOption(SkillType.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.SLOW_DIGGING.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50)));

        //其他
        addSkillConfiguration(skills, EntityType.CREEPER, 80, SkillType.EXPLODE, c ->
                c.addOption(SkillType.EXPLODE, new ExplosionConfiguration(true, 3, false, 30)));

        addSkillConfiguration(skills, EntityType.ENDERMAN, 40, SkillType.TELEPORT, c ->
                c.addOption(SkillType.TELEPORT, new TeleportConfiguration(32)));

        addSkillConfiguration(skills, EntityType.WARDEN, SonicBoomMorphSkill.defaultCooldown, SkillType.SONIC_BOOM);

        addSkillConfiguration(skills, EntityType.EVOKER, 100, SkillType.EVOKER);
    }

    public static void addAbilityConfigurations(List<SkillConfiguration> skills)
    {
        addAbilityConfiguration(skills, EntityTypeUtils.canFly(), AbilityType.CAN_FLY, c ->
        {
            var option = new FlyOption(EntityTypeUtils.getDefaultFlyingSpeed(EntityTypeUtils.fromString(c.getIdentifier())));

            option.setMinimumHunger(6);
            option.setHungerConsumeMultiplier(Math.min(option.getFlyingSpeed() / 0.05f, 2));

            c.addOption(AbilityType.CAN_FLY, option);
        });

        addAbilityConfiguration(skills, EntityTypeUtils.hasFireResistance(), AbilityType.HAS_FIRE_RESISTANCE);
        addAbilityConfiguration(skills, EntityTypeUtils.takesDamageFromWater(), AbilityType.TAKES_DAMAGE_FROM_WATER);
        addAbilityConfiguration(skills, EntityTypeUtils.canBreatheUnderWater(), AbilityType.CAN_BREATHE_UNDER_WATER);
        addAbilityConfiguration(skills, EntityTypeUtils.burnsUnderSun(), AbilityType.BURNS_UNDER_SUN);
        addAbilityConfiguration(skills, EntityTypeUtils.alwaysNightVision(), AbilityType.ALWAYS_NIGHT_VISION);
        addAbilityConfiguration(skills, EntityTypeUtils.hasJumpBoost(), AbilityType.HAS_JUMP_BOOST);
        addAbilityConfiguration(skills, EntityTypeUtils.hasSmallJumpBoost(), AbilityType.HAS_SMALL_JUMP_BOOST);
        addAbilityConfiguration(skills, EntityTypeUtils.noFallDamage(), AbilityType.NO_FALL_DAMAGE);
        addAbilityConfiguration(skills, EntityTypeUtils.hasFeatherFalling(), AbilityType.HAS_FEATHER_FALLING);

        addAbilityConfiguration(skills, EntityType.HORSE, AbilityType.ATTRIBUTE, c ->
        {
            c.addOption(AbilityType.ATTRIBUTE,
                    AttributeModifyOption.from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, 0.5d));
        });

        addAbilityConfiguration(skills, EntityType.IRON_GOLEM, AbilityType.ATTRIBUTE, c ->
        {
            c.addOption(AbilityType.ATTRIBUTE,
                    AttributeModifyOption
                            .from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                            .with(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d));
        });

        addAbilityConfiguration(skills, EntityType.WARDEN, AbilityType.ATTRIBUTE, c ->
        {
            c.addOption(AbilityType.ATTRIBUTE,
                    AttributeModifyOption
                            .from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                            .with(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d));
        });

        addAbilityConfiguration(skills, EntityTypeUtils.reducesMagicDamage(), AbilityType.REDUCES_MAGIC_DAMAGE, c ->
        {
            c.addOption(AbilityType.REDUCES_MAGIC_DAMAGE,
                    new ReduceDamageOption(0.15d, true));
        });

        addAbilityConfiguration(skills, EntityTypeUtils.reducesFallDamage(), AbilityType.REDUCES_FALL_DAMAGE, c ->
        {
            c.addOption(AbilityType.REDUCES_FALL_DAMAGE,
                    new ReduceDamageOption(10));
        });

        addAbilityConfiguration(skills, EntityTypeUtils.hasSnowTrail(), AbilityType.SNOWY);

        addAbilityConfiguration(skills, EntityTypeUtils.wardenLessAware(), AbilityType.WARDEN_LESS_AWARE);

        addAbilityConfiguration(skills, "player:" + MorphManager.disguiseFallbackName, AbilityType.CHAT_OVERRIDE, null);

        addAbilityConfiguration(skills, EntityType.WITHER, AbilityType.BOSSBAR, c ->
        {
            c.addOption(AbilityType.BOSSBAR,
                    new BossbarOption(new BossbarOption.BossbarCreateOption("<name> (<who>)", BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS, Set.of(BossBar.Flag.DARKEN_SCREEN)), 80));
        });

        addAbilityConfiguration(skills, EntityType.ENDER_DRAGON, AbilityType.BOSSBAR, c ->
        {
            c.addOption(AbilityType.BOSSBAR,
                    new BossbarOption(new BossbarOption.BossbarCreateOption("<name> (<who>)", BossBar.Color.PINK, BossBar.Overlay.PROGRESS, Set.of()), -1));
        });

        addAbilityConfiguration(skills, EntityType.FOX, AbilityType.NO_SWEET_BUSH_DAMAGE);
    }
}
