package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.skills.DefaultConfigGenerator;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.impl.LaunchProjectileMorphSkill;
import xiamomc.morph.skills.impl.SonicBoomMorphSkill;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 技能配置存储，有关技能执行、注册技能请查看{@link MorphSkillHandler} 和 {@link AbilityHandler}
 */
public class SkillAbilityConfigurationStore extends MorphJsonBasedStorage<SkillAbilityConfigurationContainer>
{
    /**
     * 配置 -> 技能
     */
    private final Map<SkillAbilityConfiguration, IMorphSkill<?>> configToSkillMap = new Object2ObjectOpenHashMap<>();

    /**
     * 获取已配置的技能
     *
     * @return 配置 -> 技能表
     */
    public Map<SkillAbilityConfiguration, IMorphSkill<?>> getConfiguredSkills()
    {
        return configToSkillMap;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "skills.json";
    }

    @Override
    protected @NotNull SkillAbilityConfigurationContainer createDefault()
    {
        return DefaultConfigGenerator.createInstance().generateConfiguration();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "技能存储";
    }

    private final int targetVersion = 24;

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityHandler abilityHandler;

    @Override
    public boolean reloadConfiguration()
    {
        var val = super.reloadConfiguration();

        var success = new AtomicBoolean(val);

        if (!success.get())
            logger.warn("We met some problem reloading, some configurations may not be added.");

        if (!val) return false;

        try
        {
            configToSkillMap.clear();
            abilityHandler.clearAbilities();

            storingObject.configurations.forEach(c ->
            {
                if (!registerConfiguration(c)) success.set(false);

                var abilities = new ObjectArrayList<IMorphAbility<?>>();

                c.getAbilitiyIdentifiers().forEach(i ->
                {
                    if (i.isEmpty()) return;

                    var key = NamespacedKey.fromString(i);

                    if (key == null)
                        logger.error("Invalid skill identifier: " + i);

                    var ability = abilityHandler.getAbility(key);

                    if (ability == null) return;

                    abilities.add(ability);

                    if (!ability.setOptionGeneric(c.getIdentifier(), c.getAbilityOptions(ability)))
                    {
                        logger.warn("Unable to add skill configuration" + c.getIdentifier() + " -> " + ability.getIdentifier());
                        success.set(false);
                    }
                });

                abilityHandler.setAbilities(c, abilities);
            });

            if (storingObject.version < targetVersion)
                success.set(migrate(storingObject) || success.get());

            saveConfiguration();
        }
        catch (Throwable t)
        {
            logger.error("Error occurred while processing skill configurations：" + t.getMessage());
            t.printStackTrace();

            configToSkillMap.clear();
            return false;
        }

        return true;
    }

    /**
     * 注册一个技能配置
     * @param configuration 要注册的配置
     * @return 操作是否成功
     */
    public boolean registerConfiguration(SkillAbilityConfiguration configuration)
    {
        if (configToSkillMap.containsKey(configuration))
            return true;

        if (configToSkillMap.keySet().stream().anyMatch(c -> c.getIdentifier().equals(configuration.getIdentifier())))
        {
            logger.error("Another configuration instance already registered as " + configuration.getIdentifier() + "!");
            return false;
        }

        var type = configuration.getSkillIdentifier();

        if (type.equals(SkillType.UNKNOWN))
        {
            logger.error(configuration + " has an invalid skill identifier");
            return false;
        }

        var skillOptional = skillHandler.getRegistedSkills().stream()
                .filter(s -> s.getIdentifier().equals(type)).findFirst();

        if (skillOptional.isEmpty())
        {
            logger.error("Unable to find any skill that matches the identifier: " + type.asString());
            return false;
        }

        configToSkillMap.put(configuration, skillOptional.get());

        return true;
    }

    private boolean migrate(SkillAbilityConfigurationContainer config)
    {
        logger.info("Updating skill configurations...");

        try
        {
            var version = config.version;

            //1: fake_inventory改名fake_equip
            if (version < 1)
            {
                var oldFakeInvKey = new NamespacedKey("morph", "fake_inventory");
                config.configurations.forEach(c ->
                {
                    if (c.getSkillIdentifier().equals(oldFakeInvKey))
                        c.setSkillIdentifier(SkillType.INVENTORY);
                });
            }

            //5: 技能设置迁移到settings中
            if (version < 5)
            {
                config.configurations.forEach(c ->
                {
                    var effect = c.getEffectConfiguration();
                    var projective = c.getProjectiveConfiguration();
                    var explosion = c.getExplosionConfiguration();
                    var teleport = c.getTeleportConfiguration();

                    c.setOption(SkillType.TELEPORT.asString(), teleport);
                    c.setOption(SkillType.APPLY_EFFECT.asString(), effect);
                    c.setOption(SkillType.LAUNCH_PROJECTILE.asString(), projective);
                    c.setOption(SkillType.EXPLODE.asString(), explosion);
                });
            }

            //9: 实现Warden的音爆技能
            if (version < 9)
            {
                var targetConfig = getConfigFor(EntityType.WARDEN, config);

                if (targetConfig != null && targetConfig.getSkillIdentifier().equals(SkillType.NONE))
                {
                    targetConfig.setSkillIdentifier(SkillType.SONIC_BOOM);
                    targetConfig.setCooldown(SonicBoomMorphSkill.defaultCooldown);
                }
            }

            //11: 调整fallback机制
            if (version < 11)
            {
                config.configurations.stream()
                        .filter(s -> s.getIdentifier().equals(EntityType.PLAYER.getKey().asString()))
                        .findFirst().ifPresent(targetConfig -> targetConfig.setIdentifier("player:" + MorphManager.disguiseFallbackName));

            }

            //马匹被动改成更改属性
            if (version < 13)
            {
                var targetConfig= getConfigFor(EntityType.HORSE, config);

                if (targetConfig != null)
                {
                    var targetIdentifier = AbilityType.HAS_SPEED_BOOST.asString();
                    targetConfig.getAbilitiyIdentifiers().removeIf(s -> s.equals(targetIdentifier));
                }
            }

            //恶魂的技能合并到弹射物中
            if (version < 14)
            {
                var ghastConfig = getConfigFor(EntityType.GHAST, config);

                if (ghastConfig != null)
                {
                    //noinspection removal
                    ghastConfig.moveOption(SkillType.GHAST, SkillType.LAUNCH_PROJECTILE);
                    ghastConfig.setSkillIdentifier(SkillType.LAUNCH_PROJECTILE);

                    var option = ghastConfig.getSkillOptions(new LaunchProjectileMorphSkill());
                    if (option != null)
                    {
                        option.put("delay", DisguiseUtils.GHAST_EXECUTE_DELAY);
                        option.put("warning_sound_name", "entity.ghast.warn");
                    }
                }
            }

            //一些拥有飞行被动的伪装同时也拥有免疫摔落伤害的被动
            if (version < 19)
            {
                for (EntityType entityType : EntityTypeUtils.noFallDamage1())
                {
                    config.configurations.stream().filter(s -> s != null && s.getIdentifier().equals(entityType.getKey().asString()))
                            .findFirst().ifPresent(cfg -> cfg.getAbilitiyIdentifiers().removeIf(s -> s.equals(AbilityType.NO_FALL_DAMAGE.asString())));
                }
            }

            //实现了女巫的抛掷技能
            if (version < 21)
            {
                var option = getConfigFor(EntityType.WITCH, config);

                if (option != null)
                {
                    if (option.getSkillIdentifier().equals(SkillType.NONE))
                    {
                        option.setSkillIdentifier(SkillType.WITCH);
                        option.setCooldown(80);
                    }
                }
            }

            //更新默认设置
            var generator = DefaultConfigGenerator.createInstance();
            generator.setConfigurationList(config.configurations);
            generator.generateSkills();
            generator.generateAbilities();

            config.version = targetVersion;

            logger.info("Done! Reloading skill configurations...");
            this.addSchedule(this::reloadConfiguration);
            return true;
        }
        catch (Throwable t)
        {
            logger.error("Error occurred while updating skill configuration: " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }

    @Nullable
    private SkillAbilityConfiguration getConfigFor(String id, SkillAbilityConfigurationContainer config)
    {
        return config.configurations.stream()
                .filter(s -> s != null && s.getIdentifier().equals(EntityType.WITCH.getKey().asString()))
                .findFirst().orElse(null);
    }

    @Nullable
    private SkillAbilityConfiguration getConfigFor(EntityType type, SkillAbilityConfigurationContainer config)
    {
        return getConfigFor(type.getKey().asString(), config);
    }
}
