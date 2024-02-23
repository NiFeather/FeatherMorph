package xiamomc.morph.skills;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.options.*;
import xiamomc.morph.skills.impl.SonicBoomMorphSkill;
import xiamomc.morph.skills.options.EffectConfiguration;
import xiamomc.morph.skills.options.ExplosionConfiguration;
import xiamomc.morph.skills.options.ProjectileConfiguration;
import xiamomc.morph.skills.options.TeleportConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfigurationContainer;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.EntityTypeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultConfigGenerator
{
    public static DefaultConfigGenerator createInstance()
    {
        return new DefaultConfigGenerator();
    }

    private List<SkillAbilityConfiguration> configurations = new ObjectArrayList<>();

    private SkillAbilityConfiguration getConfiguration(String mobId)
    {
        var cfg = configurations.stream()
                .filter(c -> c.getIdentifier().equalsIgnoreCase(mobId))
                .findFirst().orElse(null);

        if (cfg != null) return cfg;

        var newConfig = new SkillAbilityConfiguration();
        newConfig.setIdentifier(mobId)
                .setSkillIdentifier(SkillType.NONE);

        configurations.add(newConfig);

        return newConfig;
    }

    private SkillAbilityConfiguration getConfiguration(EntityType entityType)
    {
        return getConfiguration(entityType.key().asString());
    }

    @Nullable
    private SkillAbilityConfigurationContainer cachedContainer;

    public SkillAbilityConfigurationContainer generateConfiguration()
    {
        if (cachedContainer != null) return cachedContainer;

        var container = new SkillAbilityConfigurationContainer();

        this.generateSkills();
        this.generateAbilities();

        container.configurations.addAll(this.configurations);

        cachedContainer = container;

        return container;
    }

    public void setConfigurationList(List<SkillAbilityConfiguration> newConfigurations)
    {
        this.configurations = newConfigurations;
    }

    public void generateSkills()
    {
        // 伪装物品
        this.getConfiguration(EntityType.ARMOR_STAND)
                .setSkillIdentifier(SkillType.INVENTORY)
                .setCooldown(20);

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .setSkillIdentifier(SkillType.INVENTORY)
                .setCooldown(20);

        // 弹射物
        this.getConfiguration(EntityType.BLAZE)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(10)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8));

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(80)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80));

        this.getConfiguration(EntityType.LLAMA)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(25)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.TRADER_LLAMA)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(25)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8));

        this.getConfiguration(EntityType.SHULKER)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(40)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15));

        this.getConfiguration(EntityType.SNOWMAN)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(15)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8));

        this.getConfiguration(EntityType.WITHER)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(10)
                .appendOption(SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24));

        this.getConfiguration(EntityType.GHAST)
                .setSkillIdentifier(SkillType.LAUNCH_PROJECTILE)
                .setCooldown(DisguiseUtils.GHAST_EXECUTE_DELAY + 40)
                .appendOption(
                        SkillType.LAUNCH_PROJECTILE, new ProjectileConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35)
                                .withDelay(DisguiseUtils.GHAST_EXECUTE_DELAY)
                                .withWarningSound("entity.ghast.warn")
                );

        // 药效给与
        this.getConfiguration(EntityType.DOLPHIN)
                .setSkillIdentifier(SkillType.APPLY_EFFECT)
                .setCooldown(180)
                .appendOption(SkillType.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9));

        this.getConfiguration(EntityType.ELDER_GUARDIAN)
                .setSkillIdentifier(SkillType.APPLY_EFFECT)
                .setCooldown(1200)
                .appendOption(SkillType.APPLY_EFFECT, new EffectConfiguration(PotionEffectType.SLOW_DIGGING.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50));

        // 其他
        this.getConfiguration(EntityType.CREEPER)
                .setSkillIdentifier(SkillType.EXPLODE)
                .setCooldown(80)
                .appendOption(SkillType.EXPLODE, new ExplosionConfiguration(true, 3, false, 30, "entity.creeper.primed"));

        this.getConfiguration(EntityType.ENDERMAN)
                .setSkillIdentifier(SkillType.TELEPORT)
                .setCooldown(40)
                .appendOption(SkillType.TELEPORT, new TeleportConfiguration(32));

        this.getConfiguration(EntityType.WARDEN)
                .setSkillIdentifier(SkillType.SONIC_BOOM)
                .setCooldown(SonicBoomMorphSkill.defaultCooldown);

        this.getConfiguration(EntityType.EVOKER)
                .setSkillIdentifier(SkillType.EVOKER)
                .setCooldown(100);

        this.getConfiguration(EntityType.WITCH)
                .setSkillIdentifier(SkillType.WITCH)
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
                    .addAbilityIdentifier(AbilityType.CAN_FLY)
                    .appendOption(AbilityType.CAN_FLY, option);
        }

        this.setAbilityRange(EntityTypeUtils.hasFireResistance(), AbilityType.HAS_FIRE_RESISTANCE);
        this.setAbilityRange(EntityTypeUtils.takesDamageFromWater(), AbilityType.TAKES_DAMAGE_FROM_WATER);
        this.setAbilityRange(EntityTypeUtils.canBreatheUnderWater(), AbilityType.CAN_BREATHE_UNDER_WATER);
        this.setAbilityRange(EntityTypeUtils.dryOutInAir(), AbilityType.DRYOUT_IN_AIR);
        this.setAbilityRange(EntityTypeUtils.burnsUnderSun(), AbilityType.BURNS_UNDER_SUN);
        this.setAbilityRange(EntityTypeUtils.alwaysNightVision(), AbilityType.ALWAYS_NIGHT_VISION);
        this.setAbilityRange(EntityTypeUtils.noFallDamage(), AbilityType.NO_FALL_DAMAGE);
        this.setAbilityRange(EntityTypeUtils.hasJumpBoost(), AbilityType.HAS_JUMP_BOOST);
        this.setAbilityRange(EntityTypeUtils.hasSmallJumpBoost(), AbilityType.HAS_SMALL_JUMP_BOOST);
        this.setAbilityRange(EntityTypeUtils.hasFeatherFalling(), AbilityType.HAS_FEATHER_FALLING);

        this.getConfiguration(EntityType.AXOLOTL)
                .addAbilityIdentifier(AbilityType.DRYOUT_IN_AIR)
                .appendOption(AbilityType.DRYOUT_IN_AIR, new DryoutAbilityOption(false));

        this.getConfiguration(EntityType.HORSE)
                .addAbilityIdentifier(AbilityType.ATTRIBUTE)
                .appendOption(AbilityType.ATTRIBUTE, AttributeModifyOption.from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, 0.5d));

        this.getConfiguration(EntityType.IRON_GOLEM)
                .addAbilityIdentifier(AbilityType.ATTRIBUTE)
                .appendOption(
                        AbilityType.ATTRIBUTE,
                        AttributeModifyOption
                                .from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 15)
                                .with(Attribute.GENERIC_ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.55d)
                )
                .addAbilityIdentifier(AbilityType.EXTRA_KNOCKBACK)
                .appendOption(
                        AbilityType.EXTRA_KNOCKBACK,
                        ExtraKnockbackOption.from(0, 0.4D, 0)
                );

        this.getConfiguration(EntityType.WARDEN)
                .addAbilityIdentifier(AbilityType.ATTRIBUTE)
                .appendOption(
                        AbilityType.ATTRIBUTE,
                        AttributeModifyOption
                                .from(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6)
                                .with(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifyOption.OperationType.add, 1d)
                                .with(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifyOption.OperationType.add, 30)
                                .with(Attribute.GENERIC_ATTACK_SPEED, AttributeModifyOption.OperationType.multiply_base, -0.6d)
                )
                .addAbilityIdentifier(AbilityType.WARDEN);

        this.getConfiguration(EntityTypeUtils.reducesMagicDamage())
                .addAbilityIdentifier(AbilityType.REDUCES_MAGIC_DAMAGE)
                .appendOption(AbilityType.REDUCES_MAGIC_DAMAGE, new ReduceDamageOption(0.15d, true));

        this.getConfiguration(EntityTypeUtils.reducesFallDamage())
                .addAbilityIdentifier(AbilityType.REDUCES_FALL_DAMAGE)
                .appendOption(AbilityType.REDUCES_FALL_DAMAGE, new ReduceDamageOption(10));

        this.getConfiguration(EntityTypeUtils.hasSnowTrail())
                .addAbilityIdentifier(AbilityType.SNOWY);

        for (var type : EntityTypeUtils.wardenLessAware())
        {
            this.getConfiguration(type)
                    .addAbilityIdentifier(AbilityType.WARDEN_LESS_AWARE);
        }

        this.getConfiguration("player:" + MorphManager.disguiseFallbackName)
                .addAbilityIdentifier(AbilityType.CHAT_OVERRIDE);

        this.getConfiguration(EntityType.WITHER)
                .addAbilityIdentifier(AbilityType.BOSSBAR)
                .appendOption(
                        AbilityType.BOSSBAR,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PURPLE,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of(BossBar.Flag.DARKEN_SCREEN)),
                                80)
                );

        this.getConfiguration(EntityType.ENDER_DRAGON)
                .addAbilityIdentifier(AbilityType.BOSSBAR)
                .appendOption(
                        AbilityType.BOSSBAR,
                        new BossbarOption(
                                new BossbarOption.BossbarCreateOption(
                                        "<name> (<who>)",
                                        BossBar.Color.PINK,
                                        BossBar.Overlay.PROGRESS,
                                        Set.of()),
                                -1)
                )
                .addAbilityIdentifier(AbilityType.HEALS_FROM_ENTITY)
                .appendOption(
                        AbilityType.HEALS_FROM_ENTITY,
                        new HealsFromEntityOption(1, 10, 0.05d, 32d, EntityType.ENDER_CRYSTAL.key().asString()));

        this.getConfiguration(EntityType.FOX)
                        .addAbilityIdentifier(AbilityType.NO_SWEET_BUSH_DAMAGE);

        this.getConfiguration(EntityType.WITHER_SKELETON)
                .addAbilityIdentifier(AbilityType.POTION_ON_ATTACK)
                .appendOption(AbilityType.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.WITHER, 10 * 20, 0));

        this.getConfiguration(EntityType.HUSK)
                .addAbilityIdentifier(AbilityType.POTION_ON_ATTACK)
                .appendOption(AbilityType.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.HUNGER, 7 * 2 * 20, 0));

        this.getConfiguration(EntityType.CAVE_SPIDER)
                .addAbilityIdentifier(AbilityType.POTION_ON_ATTACK)
                .appendOption(AbilityType.POTION_ON_ATTACK,
                        PotionEffectOption.from(PotionEffectType.POISON, 10 * 20, 0));

        for (var type : EntityTypeUtils.spider())
            this.getConfiguration(type).addAbilityIdentifier(AbilityType.SPIDER);

        this.getConfiguration(EntityType.TURTLE)
                .addAbilityIdentifier(AbilityType.CAN_BREATHE_UNDER_WATER);
    }
}
