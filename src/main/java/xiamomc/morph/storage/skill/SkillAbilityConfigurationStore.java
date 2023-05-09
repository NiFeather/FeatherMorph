package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.skills.DefaultConfigGenerator;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.impl.LaunchProjectiveMorphSkill;
import xiamomc.morph.skills.impl.SonicBoomMorphSkill;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.morph.utilities.DisguiseUtils;
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
        return DefaultConfigGenerator.getDefaultSkillConfiguration();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "技能存储";
    }

    private final int targetVersion = 17;

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityHandler abilityHandler;

    @Override
    public boolean reloadConfiguration()
    {
        var val = super.reloadConfiguration();

        var success = new AtomicBoolean(val);

        if (val)
        {
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
                        var key = NamespacedKey.fromString(i);

                        if (key == null)
                            logger.error("Invalid skill identifier: " + i);

                        var ability = abilityHandler.getAbility(key);

                        if (ability != null)
                        {
                            abilities.add(ability);

                            if (!ability.setOptionGeneric(c.getIdentifier(), c.getAbilityOptions(ability)))
                            {
                                logger.warn("Unable to add skill configuration" + c.getIdentifier() + " -> " + ability.getIdentifier());
                                success.set(false);
                            }
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
        }

        if (!success.get())
            logger.warn("We met some problem reloading, some configurations may not be added.");

        return val;
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
                    c.setOption(SkillType.LAUNCH_PROJECTIVE.asString(), projective);
                    c.setOption(SkillType.EXPLODE.asString(), explosion);
                });
            }

            //9: 实现Warden的音爆技能
            if (version < 9)
            {
                var targetConfig = config.configurations.stream()
                        .filter(s -> s != null && s.getIdentifier().equals(EntityType.WARDEN.getKey().asString()))
                        .findFirst().orElse(null);

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
                        .findFirst().ifPresent(targetConfig -> targetConfig.setIdentifier("player:@default"));

            }

            //恶魂的技能变成延迟释放
            if (version < 12)
            {
                var targetConfig = config.configurations.stream()
                        .filter(s -> s != null && s.getIdentifier().equals(EntityType.GHAST.getKey().asString()))
                        .findFirst().orElse(null);

                if (targetConfig != null)
                {
                    if (targetConfig.getSkillIdentifier().equals(SkillType.LAUNCH_PROJECTIVE))
                    {
                        if (targetConfig.getCooldown() == 40)
                            targetConfig.setCooldown(DisguiseUtils.GHAST_EXECUTE_DELAY + 40);

                        targetConfig.setSkillIdentifier(SkillType.GHAST);
                        targetConfig.moveOption(SkillType.LAUNCH_PROJECTIVE, SkillType.GHAST);
                    }
                }
            }

            //马匹被动改成更改属性
            if (version < 13)
            {
                var targetConfig= config.configurations.stream()
                        .filter(s -> s != null && s.getIdentifier().equals(EntityType.HORSE.getKey().asString()))
                        .findFirst().orElse(null);

                if (targetConfig != null)
                {
                    var targetIdentifier = AbilityType.HAS_SPEED_BOOST.asString();
                    targetConfig.getAbilitiyIdentifiers().removeIf(s -> s.equals(targetIdentifier));
                }
            }

            //恶魂的技能合并到弹射物中
            if (version < 14)
            {
                var ghastConfig = config.configurations.stream()
                        .filter(s -> s != null && s.getIdentifier().equals(EntityType.GHAST.getKey().asString()))
                        .findFirst().orElse(null);

                if (ghastConfig != null)
                {
                    ghastConfig.moveOption(SkillType.GHAST, SkillType.LAUNCH_PROJECTIVE);
                    ghastConfig.setSkillIdentifier(SkillType.LAUNCH_PROJECTIVE);

                    var option = ghastConfig.getSkillOptions(new LaunchProjectiveMorphSkill());
                    if (option != null)
                    {
                        option.put("delay", DisguiseUtils.GHAST_EXECUTE_DELAY);
                        option.put("warning_sound_name", "entity.ghast.warn");
                    }
                }
            }

            //更新默认设置
            DefaultConfigGenerator.addAbilityConfigurations(config.configurations);
            DefaultConfigGenerator.addSkillConfigurations(config.configurations);

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
}
