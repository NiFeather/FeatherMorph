package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.skills.DefaultConfigGenerator;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.impl.SonicBoomMorphSkill;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 技能配置存储，有关技能执行、注册技能请查看{@link MorphSkillHandler} 和 {@link AbilityHandler}
 */
public class SkillConfigurationStore extends MorphJsonBasedStorage<SkillConfigurationContainer>
{
    /**
     * 配置 -> 技能
     */
    private final Map<SkillConfiguration, IMorphSkill<?>> configToSkillMap = new Object2ObjectOpenHashMap<>();

    /**
     * 获取已配置的技能
     *
     * @return 配置 -> 技能表
     */
    public Map<SkillConfiguration, IMorphSkill<?>> getConfiguredSkills()
    {
        return configToSkillMap;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "skills.json";
    }

    @Override
    protected @NotNull SkillConfigurationContainer createDefault()
    {
        return DefaultConfigGenerator.getDefaultSkillConfiguration();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "技能存储";
    }

    private final int targetVersion = 10;

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
                            logger.error("无效的技能ID: " + i);

                        var ability = abilityHandler.getAbility(key);

                        if (ability != null)
                        {
                            abilities.add(ability);

                            if (!ability.setOptionGeneric(c.getIdentifier(), c.getAbilityOptions(ability)))
                            {
                                logger.warn("无法为" + c.getIdentifier() + " -> " + ability.getIdentifier() + "添加技能设置");
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
                logger.error("处理配置时出现异常：" + t.getMessage());
                t.printStackTrace();

                configToSkillMap.clear();
                return false;
            }
        }

        if (!success.get())
            logger.warn("重新加载时出现问题，一些配置将不会被添加，请查看日志排查原因。");

        return val;
    }

    /**
     * 注册一个技能配置
     * @param configuration 要注册的配置
     * @return 操作是否成功
     */
    public boolean registerConfiguration(SkillConfiguration configuration)
    {
        if (configToSkillMap.containsKey(configuration))
        {
            logger.error("已经注册过一个" + configuration + "的配置了");
            return false;
        }

        if (configToSkillMap.keySet().stream().anyMatch(c -> c.getIdentifier().equals(configuration.getIdentifier())))
        {
            logger.error("已经有一个" + configuration.getIdentifier() + "的技能了");
            return false;
        }

        var type = configuration.getSkillIdentifier();

        if (type.equals(SkillType.UNKNOWN))
        {
            logger.error(configuration + "的技能ID无效");
            return false;
        }

        var skillOptional = skillHandler.getRegistedSkills().stream()
                .filter(s -> s.getIdentifier().equals(type)).findFirst();

        if (skillOptional.isEmpty())
        {
            logger.error("找不到和" + type + "匹配的技能");
            return false;
        }

        configToSkillMap.put(configuration, skillOptional.get());

        return true;
    }

    private boolean migrate(SkillConfigurationContainer config)
    {
        logger.info("正在更新技能配置...");

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

            //更新默认设置
            DefaultConfigGenerator.addAbilityConfigurations(config.configurations);
            DefaultConfigGenerator.addSkillConfigurations(config.configurations);

            config.version = targetVersion;

            logger.info("已更新技能配置，即将重载存储...");
            this.addSchedule(this::reloadConfiguration);
            return true;
        }
        catch (Throwable t)
        {
            logger.error("更新配置时出现问题：" + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }
}
