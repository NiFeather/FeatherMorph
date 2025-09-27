package xyz.nifeather.morph.skills;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.AbilityManager;
import xyz.nifeather.morph.abilities.impl.FlyAbility;
import xyz.nifeather.morph.abilities.options.*;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.skills.impl.SonicBoomMorphSkill;
import xyz.nifeather.morph.skills.options.EffectConfiguration;
import xyz.nifeather.morph.skills.options.ExplosionConfiguration;
import xyz.nifeather.morph.skills.options.ProjectileConfiguration;
import xyz.nifeather.morph.skills.options.TeleportConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;
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

    private Map<String, SkillAbilityConfigContainer> configurations = new Object2ObjectOpenHashMap<>();

    private SkillAbilityConfigContainer getConfiguration(String mobId)
    {
        var cfg = configurations.getOrDefault(mobId, null);

        if (cfg != null) return cfg;

        var newConfig = new SkillAbilityConfigContainer();
        newConfig.setSkillIdentifier(SkillNames.NONE);

        configurations.put(mobId, newConfig);

        return newConfig;
    }

    private SkillAbilityConfigContainer getConfiguration(EntityType entityType)
    {
        return getConfiguration(entityType.key().asString());
    }

    public Map<String, SkillAbilityConfigContainer> generateConfiguration()
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
                .setSkillCooldown(20);

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .setSkillIdentifier(SkillNames.FAKE_EQUIP)
                .setSkillCooldown(20);

        // 弹射物
        this.getConfiguration(EntityType.BLAZE)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(10)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8));

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(80)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80));

        this.getConfiguration(EntityType.LLAMA)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(25)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.TRADER_LLAMA)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(25)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.SHULKER)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(40)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15));

        this.getConfiguration(EntityType.SNOW_GOLEM)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(15)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8));

        this.getConfiguration(EntityType.WITHER)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(10)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24));

        this.getConfiguration(EntityType.GHAST)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(DisguiseUtils.GHAST_EXECUTE_DELAY + 40)
                .appendOption(
                        SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35, DisguiseUtils.GHAST_EXECUTE_DELAY)
                                .withWarningSound("entity.ghast.warn")
                );

        this.getConfiguration(EntityType.BREEZE)
                .setSkillIdentifier(SkillNames.LAUNCH_PROJECTILE)
                .setSkillCooldown(40)
                .appendOption(SkillNames.LAUNCH_PROJECTILE, ProjectileConfiguration.OPTION_HANDLER, new ProjectileConfiguration(EntityType.WIND_CHARGE, 1, "entity.breeze.shoot", 16));

        // 药效给与
        this.getConfiguration(EntityType.DOLPHIN)
                .setSkillIdentifier(SkillNames.APPLY_EFFECT)
                .setSkillCooldown(180)
                .appendOption(SkillNames.APPLY_EFFECT, EffectConfiguration.OPTION_HANDLER, new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9));

        this.getConfiguration(EntityType.ELDER_GUARDIAN)
                .setSkillIdentifier(SkillNames.APPLY_EFFECT)
                .setSkillCooldown(1200)
                .appendOption(SkillNames.APPLY_EFFECT, EffectConfiguration.OPTION_HANDLER, new EffectConfiguration(PotionEffectType.MINING_FATIGUE.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50));

        // 其他
        this.getConfiguration(EntityType.CREEPER)
                .setSkillIdentifier(SkillNames.EXPLODE)
                .setSkillCooldown(80)
                .appendOption(SkillNames.EXPLODE, ExplosionConfiguration.OPTION_HANDLER, new ExplosionConfiguration(true, 3, false, 30, "entity.creeper.primed"));

        this.getConfiguration(EntityType.ENDERMAN)
                .setSkillIdentifier(SkillNames.TELEPORT)
                .setSkillCooldown(40)
                .appendOption(SkillNames.TELEPORT, TeleportConfiguration.OPTION_HANDLER, new TeleportConfiguration(32));

        this.getConfiguration(EntityType.WARDEN)
                .setSkillIdentifier(SkillNames.SONIC_BOOM)
                .setSkillCooldown(SonicBoomMorphSkill.defaultCooldown);

        this.getConfiguration(EntityType.EVOKER)
                .setSkillIdentifier(SkillNames.EVOKER)
                .setSkillCooldown(100);

        this.getConfiguration(EntityType.WITCH)
                .setSkillIdentifier(SkillNames.WITCH)
                .setSkillCooldown(80);

        this.getConfiguration(EntityType.GUARDIAN)
                .setSkillIdentifier(SkillNames.GUARDIAN)
                .setSkillCooldown(80);

        this.getConfiguration(EntityType.MANNEQUIN)
                .setSkillIdentifier(SkillNames.FAKE_EQUIP)
                .setSkillCooldown(20);
    }

    private void setAbilityRange(Collection<EntityType> types, NamespacedKey abilityType)
    {
        for (var type : types)
            this.getConfiguration(type).addAbility(abilityType);
    }

    public void generateAbilities()
    {
        for (var type : EntityTypeUtils.canFly())
        {
            var option = new FlyOption(EntityTypeUtils.getDefaultFlyingSpeed(type));

            this.getConfiguration(type)
                    .addAbility(AbilityNames.CAN_FLY)
                    .appendOption(AbilityNames.CAN_FLY, FlyOption.OPTION_HANDLER, option);
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
                .addAbility(AbilityNames.DRYOUT_IN_AIR)
                .appendOption(AbilityNames.DRYOUT_IN_AIR, DryoutAbilityOption.OPTION_HANDLER, new DryoutAbilityOption(false));

        this.getConfiguration(EntityType.HORSE)
                .addAbility(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(AbilityNames.ATTRIBUTE_MODIFY,
                        AttributeModifyOption.OPTION_HANDLER,
                        AttributeModifyOption
                                .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, 0.5d)
                                .with(Attribute.STEP_HEIGHT, AttributeModifyOption.OperationType.add, 0.4d));

        this.getConfiguration(EntityType.IRON_GOLEM)
                .addAbility(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(
                        AbilityNames.ATTRIBUTE_MODIFY,
                        AttributeModifyOption.OPTION_HANDLER,
                        AttributeModifyOption
                                .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 15)
                                .with(Attribute.ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.55d)
                )
                .addAbility(AbilityNames.EXTRA_KNOCKBACK)
                .appendOption(
                        AbilityNames.EXTRA_KNOCKBACK,
                        ExtraKnockbackOption.OPTION_HANDLER,
                        ExtraKnockbackOption.from(0, 0.8D, 0)
                );

        this.getConfiguration(EntityType.WARDEN)
                .addAbility(AbilityNames.ATTRIBUTE_MODIFY)
                .appendOption(
                        AbilityNames.ATTRIBUTE_MODIFY,
                        AttributeModifyOption.OPTION_HANDLER,
                        AttributeModifyOption
                                .from(Attribute.MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 30)
                                .with(Attribute.ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6d)
                )
                .addAbility(AbilityNames.WARDEN);

        this.getConfiguration(EntityTypeUtils.reducesMagicDamage())
                .addAbility(AbilityNames.REDUCES_MAGIC_DAMAGE)
                .appendOption(AbilityNames.REDUCES_MAGIC_DAMAGE, ReduceDamageOption.OPTION_HANDLER, new ReduceDamageOption(0.15d, true));

        this.getConfiguration(EntityTypeUtils.reducesFallDamage())
                .addAbility(AbilityNames.REDUCES_FALL_DAMAGE)
                .appendOption(AbilityNames.REDUCES_FALL_DAMAGE, ReduceDamageOption.OPTION_HANDLER, new ReduceDamageOption(10));

        this.getConfiguration(EntityTypeUtils.hasSnowTrail())
                .addAbility(AbilityNames.SNOWY);

        for (var type : EntityTypeUtils.wardenLessAware())
        {
            this.getConfiguration(type)
                    .addAbility(AbilityNames.WARDEN_LESS_AWARE);
        }

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .addAbility(AbilityNames.CHAT_OVERRIDE);

        this.getConfiguration(EntityType.WITHER)
                .addAbility(AbilityNames.BOSSBAR)
                .appendOption(
                        AbilityNames.BOSSBAR,
                        BossbarOption.OPTION_HANDLER,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PURPLE,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of(BossBar.Flag.DARKEN_SCREEN)),
                                80)
                );

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .addAbility(AbilityNames.BOSSBAR)
                .appendOption(
                        AbilityNames.BOSSBAR,
                        BossbarOption.OPTION_HANDLER,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PINK,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of()),
                                -1)
                )
                .addAbility(AbilityNames.HEALS_FROM_ENTITY)
                .appendOption(
                        AbilityNames.HEALS_FROM_ENTITY,
                        HealsFromEntityOption.OPTION_HANDLER,
                        new HealsFromEntityOption(1, 10, 0.05d, 32d, EntityType.END_CRYSTAL));

        this.getConfiguration(EntityType.FOX)
                        .addAbility(AbilityNames.NO_SWEET_BUSH_DAMAGE);

        this.getConfiguration(EntityType.WITHER_SKELETON)
                .addAbility(AbilityNames.POTION_ON_ATTACK)
                .addAbility(AbilityNames.REDUCES_WITHER_DAMAGE)
                .addAbility(AbilityNames.HAS_FIRE_RESISTANCE)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.OPTION_HANDLER,
                        PotionEffectOption.from(PotionEffectType.WITHER, 10 * 20, 0))
                .appendOption(AbilityNames.REDUCES_WITHER_DAMAGE,
                        ReduceDamageOption.OPTION_HANDLER,
                        new ReduceDamageOption(1, true));

        this.getConfiguration(EntityType.HUSK)
                .addAbility(AbilityNames.POTION_ON_ATTACK)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.OPTION_HANDLER,
                        PotionEffectOption.from(PotionEffectType.HUNGER, 7 * 2 * 20, 0));

        this.getConfiguration(EntityType.CAVE_SPIDER)
                .addAbility(AbilityNames.POTION_ON_ATTACK)
                .appendOption(AbilityNames.POTION_ON_ATTACK,
                        PotionEffectOption.OPTION_HANDLER,
                        PotionEffectOption.from(PotionEffectType.POISON, 10 * 20, 0));

        for (var type : EntityTypeUtils.spider())
            this.getConfiguration(type).addAbility(AbilityNames.SPIDER);

        this.getConfiguration(EntityType.TURTLE)
                .addAbility(AbilityNames.CAN_BREATHE_UNDER_WATER);

        this.getConfiguration(EntityType.BREEZE)
                .addAbility(AbilityNames.HAS_JUMP_BOOST)
                .addAbility(AbilityNames.NO_FALL_DAMAGE);

        this.getConfiguration(EntityType.HAPPY_GHAST)
                .addAbility(AbilityNames.CAN_FLY)
                .appendOption(AbilityNames.CAN_FLY,
                        FlyOption.OPTION_HANDLER,
                        new FlyOption(0.05f));

        this.getConfiguration(EntityType.AXOLOTL)
                .addAbility(AbilityNames.EXTRA_AIR)
                .appendOption(AbilityNames.EXTRA_AIR,
                        ExtraAirOption.OPTION_HANDLER,
                        new ExtraAirOption(6000)); // See NMS Axolotl#getDefaultMaxAirSupply

        this.getConfiguration(EntityType.DOLPHIN)
                .addAbility(AbilityNames.EXTRA_AIR)
                .appendOption(AbilityNames.EXTRA_AIR,
                        ExtraAirOption.OPTION_HANDLER,
                        new ExtraAirOption(4800)); // See NMS Dolphin#getDefaultMaxAirSupply
    }
}
