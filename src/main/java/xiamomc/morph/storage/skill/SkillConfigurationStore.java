package xiamomc.morph.storage.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.DefaultConfigGenerator;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 技能配置存储，有关技能执行、注册技能请查看{@link MorphSkillHandler}
 */
public class SkillConfigurationStore extends MorphJsonBasedStorage<SkillConfigurationContainer>
{
    /**
     * 配置 -> 技能
     */
    private final Map<SkillConfiguration, IMorphSkill> configToSkillMap = new ConcurrentHashMap<>();

    /**
     * 获取已配置的技能
     *
     * @return 配置 -> 技能表
     */
    public Map<SkillConfiguration, IMorphSkill> getConfiguredSkills()
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
        return DefaultConfigGenerator.getDefaultConfiguration();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "技能存储";
    }

    private final int targetVersion = 1;

    @Override
    public boolean reloadConfiguration()
    {
        var val = super.reloadConfiguration();

        var success = new AtomicBoolean(true);

        try
        {
            configToSkillMap.clear();

            storingObject.configurations.forEach(c ->
            {
                if (!registerConfiguration(c)) success.set(false);
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

        if (!success.get())
            logger.warn("重新加载时出现问题，请查看log排查原因。");

        return val;
    }

    @Resolved
    private MorphSkillHandler skillHandler;

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
            //0 -> 1
            var oldFakeInvKey = new NamespacedKey("morph", "fake_inventory");
            config.configurations.forEach(c ->
            {
                if (c.getSkillIdentifier().equals(oldFakeInvKey))
                    c.setSkillIdentifier(SkillType.INVENTORY);
            });

            config.version = targetVersion;

            logger.info("已更新技能配置，即将重载存储...");
            this.addSchedule(c -> reloadConfiguration());
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
