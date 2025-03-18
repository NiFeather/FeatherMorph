package xyz.nifeather.morph.skills;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.abilities.options.*;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.skills.impl.SonicBoomMorphSkill;
import xyz.nifeather.morph.skills.options.EffectConfiguration;
import xyz.nifeather.morph.skills.options.ExplosionConfiguration;
import xyz.nifeather.morph.skills.options.ProjectileConfiguration;
import xyz.nifeather.morph.skills.options.TeleportConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DefaultConfigGenerator
{
    public static DefaultConfigGenerator createInstance()
    {
        return new DefaultConfigGenerator();
    }

    private Map<String, SkillAbilityConfiguration> configurations = new Object2ObjectOpenHashMap<>();

    private SkillAbilityConfiguration getConfiguration(String mobId)
    {
        var cfg = configurations.getOrDefault(mobId, null);

        if (cfg != null) return cfg;

        var newConfig = new SkillAbilityConfiguration();
        newConfig.setSkillIdentifier(SkillNames.NONE);

        configurations.put(mobId, newConfig);

        return newConfig;
    }

    private SkillAbilityConfiguration getConfiguration(EntityType entityType)
    {
        return getConfiguration(entityType.key().asString());
    }

    public Map<String, SkillAbilityConfiguration> generateConfiguration()
    {
        this.generateSkills();
        this.generateAbilities();

        return this.configurations;
    }

    public void generateSkills()
    {
        // 伪装物品
        this.getConfiguration(EntityType.ARMOR_STAND)
                .setSkillIdentifier(SkillNames.FAKE_EQUIP)
                .setCooldown(20);

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .setSkillIdentifier(SkillNames.FAKE_EQUIP)
                .setCooldown(20);

        // 弹射物
        this.getConfiguration(EntityType.BLAZE)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(10)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8));

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(80)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80));

        this.getConfiguration(EntityType.LLAMA)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(25)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.TRADER_LLAMA)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(25)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.SHULKER)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(40)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15));

        this.getConfiguration(EntityType.SNOW_GOLEM)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(15)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8));

        this.getConfiguration(EntityType.WITHER)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(10)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24));

        this.getConfiguration(EntityType.GHAST)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(DisguiseUtils.GHAST_EXECUTE_DELAY + 40)
                .appendOption(
                        SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35)
                                .withDelay(DisguiseUtils.GHAST_EXECUTE_DELAY)
                                .withWarningSound("entity.ghast.warn")
                );

        this.getConfiguration(EntityType.BREEZE)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setCooldown(40)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.WIND_CHARGE, 1, "entity.breeze.shoot", 16));

        // 药效给与
        this.getConfiguration(EntityType.DOLPHIN)
                .setSkillIdentifier(SkillNames.APPLY_EFFECT)
                .setCooldown(180)
                .appendOption(SkillNames.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9));

        this.getConfiguration(EntityType.ELDER_GUARDIAN)
                .setSkillIdentifier(SkillNames.APPLY_EFFECT)
                .setCooldown(1200)
                .appendOption(SkillNames.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.MINING_FATIGUE.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50));

        // 其他
        this.getConfiguration(EntityType.CREEPER)
                .setSkillIdentifier(SkillNames.EXPLODE)
                .setCooldown(80)
                .appendOption(SkillNames.EXPLODE, new ExplosionConfiguration(true, 3, false, 30, "entity.creeper.primed"));

        this.getConfiguration(EntityType.ENDERMAN)
                .setSkillIdentifier(SkillNames.TELEPORT)
                .setCooldown(40)
                .appendOption(SkillNames.TELEPORT, new TeleportConfiguration(32));

        this.getConfiguration(EntityType.WARDEN)
                .setSkillIdentifier(SkillNames.SONIC_BOOM)
                .setCooldown(SonicBoomMorphSkill.defaultCooldown);

        this.getConfiguration(EntityType.EVOKER)
                .setSkillIdentifier(SkillNames.EVOKER)
                .setCooldown(100);

        this.getConfiguration(EntityType.WITCH)
                .setSkillIdentifier(SkillNames.WITCH)
                .setCooldown(80);
    }

    private void setAbilityRange(Collection<EntityType> types, NamespacedKey abilityType)
    {
        for (var type : types)
            this.getConfiguration(type).addAbilityIdentifier(abilityType);
    }

    public void generateAbilities()
    {
        for (var type : EntityTypeUtils.canFly())
        {
            var option = new FlyOption(EntityTypeUtils.getDefaultFlyingSpeed(type));
            option.setMinimumHunger(6);
            option.setHungerConsumeMultiplier(Math.min(option.getFlyingSpeed() / 0.05f, 2));

            this.getConfiguration(type)
                    .addAbilityIdentifier(AbilityNames.CAN_FLY)
                    .appendOption(AbilityNames.CAN_FLY, option);
        }

        this.setAbilityRange(EntityTypeUtils.hasFireResistance(), AbilityNames.HAS_FIRE_RESISTANCE);
        this.setAbilityRange(EntityTypeUtils.takesDamageFromWater(), AbilityNames.TAKES_DAMAGE_FROM_WATER);
        this.setAbilityRange(EntityTypeUtils.canBreatheUnderWater(), AbilityNames.CAN_BREATHE_UNDER_WATER);
        this.setAbilityRange(EntityTypeUtils.dryOutInAir(), AbilityNames.DRYOUT_IN_AIR);
        this.setAbilityRange(EntityTypeUtils.burnsUnderSun(), AbilityNames.BURNS_UNDER_SUN);
        this.setAbilityRange(EntityTypeUtils.alwaysNightVision(), AbilityNames.ALWAYS_NIGHT_VISION);
        this.setAbilityRange(EntityTypeUtils.noFallDamage(), AbilityNames.NO_FALL_DAMAGE);
        this.setAbilityRange(EntityTypeUtils.hasJumpBoost(), AbilityNames.HAS_JUMP_BOOST);
        this.setAbilityRange(EntityTypeUtils.hasSmallJumpBoost(), AbilityNames.HAS_SMALL_JUMP_BOOST);
        this.setAbilityRange(EntityTypeUtils.hasFeatherFalling(), AbilityNames.HAS_FEATHER_FALLING);

        this.getConfiguration(EntityType.AXOLOTL)
                .addAbilityIdentifier(AbilityNames.DRYOUT_IN_AIR)
                .appendOption(AbilityNames.DRYOUT_IN_AIR, new DryoutAbilityOption(false));

        this.getConfiguration(EntityType.HORSE)
                .addAbilityIdentifier(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(AbilityNames.ATTRIBUTE_MODIFY, AttributeModifyOption
                        .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, 0.5d)
                        .with(Attribute.STEP_HEIGHT, AttributeModifyOption.OperationType.add, 0.4d));

        this.getConfiguration(EntityType.IRON_GOLEM)
                .addAbilityIdentifier(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(
                        AbilityNames.ATTRIBUTE_MODIFY,
                        AttributeModifyOption
                                .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 15)
                                .with(Attribute.ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.55d)
                )
                .addAbilityIdentifier(AbilityNames.EXTRA_KNOCKBACK)
                .appendOption(
                        AbilityNames.EXTRA_KNOCKBACK,
                        ExtraKnockbackOption.from(0, 0.8D, 0)
                );

        this.getConfiguration(EntityType.WARDEN)
                .addAbilityIdentifier(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(
                        AbilityNames.ATTRIBUTE_MODIFY,
                        AttributeModifyOption
                                .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 30)
                                .with(Attribute.ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6d)
                )
                .addAbilityIdentifier(AbilityNames.WARDEN);

        this.getConfiguration(EntityTypeUtils.reducesMagicDamage())
                .addAbilityIdentifier(AbilityNames.REDUCES_MAGIC_DAMAGE)
                .appendOption(AbilityNames.REDUCES_MAGIC_DAMAGE, new ReduceDamageOption(0.15d, true));

        this.getConfiguration(EntityTypeUtils.reducesFallDamage())
                .addAbilityIdentifier(AbilityNames.REDUCES_FALL_DAMAGE)
                .appendOption(AbilityNames.REDUCES_FALL_DAMAGE, new ReduceDamageOption(10));

        this.getConfiguration(EntityTypeUtils.hasSnowTrail())
                .addAbilityIdentifier(AbilityNames.SNOWY);

        for (var type : EntityTypeUtils.wardenLessAware())
        {
            this.getConfiguration(type)
                    .addAbilityIdentifier(AbilityNames.WARDEN_LESS_AWARE);
        }

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .addAbilityIdentifier(AbilityNames.CHAT_OVERRIDE);

        this.getConfiguration(EntityType.WITHER)
                .addAbilityIdentifier(AbilityNames.BOSSBAR)
                .appendOption(
                        AbilityNames.BOSSBAR,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PURPLE,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of(BossBar.Flag.DARKEN_SCREEN)),
                                80)
                );

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .addAbilityIdentifier(AbilityNames.BOSSBAR)
                .appendOption(
                        AbilityNames.BOSSBAR,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PINK,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of()),
                                -1)
                )
                .addAbilityIdentifier(AbilityNames.HEALS_FROM_ENTITY)
                .appendOption(
                        AbilityNames.HEALS_FROM_ENTITY,
                        new HealsFromEntityOption(1, 10, 0.05d, 32d, EntityType.END_CRYSTAL.key().asString()));

        this.getConfiguration(EntityType.FOX)
                        .addAbilityIdentifier(AbilityNames.NO_SWEET_BUSH_DAMAGE);

        this.getConfiguration(EntityType.WITHER_SKELETON)
                .addAbilityIdentifier(AbilityNames.POTION_ON_ATTACK)
                .addAbilityIdentifier(AbilityNames.REDUCES_WITHER_DAMAGE)
                .addAbilityIdentifier(AbilityNames.HAS_FIRE_RESISTANCE)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.WITHER, 10 * 20, 0))
                .appendOption(AbilityNames.REDUCES_WITHER_DAMAGE,
                        new ReduceDamageOption(1, true));

        this.getConfiguration(EntityType.HUSK)
                .addAbilityIdentifier(AbilityNames.POTION_ON_ATTACK)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.HUNGER, 7 * 2 * 20, 0));

        this.getConfiguration(EntityType.CAVE_SPIDER)
                .addAbilityIdentifier(AbilityNames.POTION_ON_ATTACK)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.POISON, 10 * 20, 0));

        for (var type : EntityTypeUtils.spider())
            this.getConfiguration(type).addAbilityIdentifier(AbilityNames.SPIDER);

        this.getConfiguration(EntityType.TURTLE)
                .addAbilityIdentifier(AbilityNames.CAN_BREATHE_UNDER_WATER);

        this.getConfiguration(EntityType.BREEZE)
                .addAbilityIdentifier(AbilityNames.HAS_JUMP_BOOST)
                .addAbilityIdentifier(AbilityNames.NO_FALL_DAMAGE);
    }
}
