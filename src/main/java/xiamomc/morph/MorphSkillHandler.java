package xiamomc.morph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.DefaultConfigGenerator;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.configurations.SkillConfiguration;
import xiamomc.morph.skills.configurations.SkillConfigurationContainer;
import xiamomc.morph.skills.impl.*;
import xiamomc.morph.storage.MorphJsonBasedStorage;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import javax.naming.Name;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphSkillHandler extends MorphJsonBasedStorage<SkillConfigurationContainer>
{
    /**
     * 配置 -> 技能
     */
    private final Map<SkillConfiguration, IMorphSkill> configToSkillMap = new ConcurrentHashMap<>();

    /**
     * 已注册的技能
     */
    private final List<IMorphSkill> skills = new ArrayList<>();

    /**
     * 玩家 -> 此玩家的CD列表
     */
    private final Map<UUID, List<SkillCooldownInfo>> uuidInfoMap = new LinkedHashMap<>();

    /**
     * 玩家 -> 当前CD
     */
    private final Map<UUID, SkillCooldownInfo> uuidCooldownMap = new LinkedHashMap<>();

    @Resolved
    private MorphManager manager;

    public MorphSkillHandler()
    {
        registerSkills(List.of(
                new ApplyEffectMorphSkill(),
                new ExplodeMorphSkill(),
                new InventoryMorphSkill(),
                new LaunchProjectiveMorphSkill(),
                new SummonFangsMorphSkill(),
                new TeleportMorphSkill(),
                new NoneMorphSkill()
        ));
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
        catch (Throwable e)
        {
            logger.error("处理配置时出现异常：" + e.getMessage());
            configToSkillMap.clear();
            e.printStackTrace();
            return false;
        }

        if (!success.get())
            logger.warn("重新加载时出现问题，请查看log排查原因。");

        return val;
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

    /**
     * 注册一批技能
     * @param skills 技能列表
     * @return 所有操作是否成功
     */
    public boolean registerSkills(List<IMorphSkill> skills)
    {
        var success = new AtomicBoolean(true);

        skills.forEach(s ->
        {
            if (!registerSkill(s)) success.set(false);
        });

        return success.get();
    }

    /**
     * 注册一个技能
     * @param skill 技能
     * @return 操作是否成功
     */
    public boolean registerSkill(IMorphSkill skill)
    {
        if (skills.contains(skill))
        {
            logger.error("已经注册过一个" + skill + "的技能了");
            return false;
        }

        if (skill.getIdentifier().equals(SkillType.UNKNOWN))
        {
            logger.error("技能ID不能是" + SkillType.UNKNOWN);
            return false;
        }

        skills.add(skill);
        return true;
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

        var skillOptional = skills.stream()
                .filter(s -> s.getIdentifier().equals(type)).findFirst();

        if (skillOptional.isEmpty())
        {
            logger.error("找不到和" + type + "匹配的技能");
            return false;
        }

        configToSkillMap.put(configuration, skillOptional.get());

        return true;
    }

    @Initializer
    private void load()
    {
        this.addSchedule(c -> this.update());
    }

    private void update()
    {
        //更新CD
        uuidCooldownMap.forEach((u, c) -> c.setCooldown(c.getCooldown() - 1));
        this.addSchedule(c -> this.update());
    }

    /**
     * 获取某个ID对应的技能和技能配置
     * @param identifier ID
     * @return 对应的技能和技能配置，如果没找到则是null
     */
    @Nullable
    private Map.Entry<SkillConfiguration, IMorphSkill> getSkillEntry(String identifier)
    {
        return configToSkillMap.entrySet().stream()
                .filter(d -> identifier.equals(d.getKey().getIdentifier())).findFirst().orElse(null);
    }

    /**
     * 让某个玩家执行伪装技能
     *
     * @param player 目标玩家
     */
    public void executeDisguiseSkill(Player player)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        var entry = getSkillEntry(state.getSkillIdentifier());

        if (entry != null && !entry.getKey().getSkillIdentifier().equals(SkillType.NONE))
        {
            var skill = entry.getValue();
            var config = entry.getKey();

            var cd = getCooldownInfo(player.getUniqueId(), state.getSkillIdentifier());
            assert cd != null;

            cd.setCooldown(skill.executeSkill(player, config));
            cd.setLastInvoke(plugin.getCurrentTick());

            if (!state.haveCooldown()) state.setCooldownInfo(cd);
        }
        else
        {
            player.sendMessage(MessageUtils.prefixes(player, SkillStrings.skillNotAvaliableString()));

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));
        }
    }

    /**
     * 获取技能冷却
     *
     * @param uuid 玩家UUID
     * @param type 技能ID
     * @return 技能信息，为null则传入的实体类型是null
     */
    @Nullable
    public SkillCooldownInfo getCooldownInfo(UUID uuid, @Nullable String type)
    {
        if (type == null) return null;

        //获取cd列表
        List<SkillCooldownInfo> infos;
        SkillCooldownInfo cdInfo;

        //获取或创建CD列表
        if (!uuidInfoMap.containsKey(uuid)) uuidInfoMap.put(uuid, infos = new ArrayList<>());
        else infos = uuidInfoMap.get(uuid);

        //获取或创建CD
        var cd = infos.stream()
                .filter(i -> i.getIdentifier().equals(type)).findFirst().orElse(null);

        if (cd == null)
        {
            cdInfo = new SkillCooldownInfo(type);
            infos.add(cdInfo);
        }
        else
            cdInfo = cd;

        return cdInfo;
    }

    /**
     * 为不活跃的CD信息计算当前刻的CD值
     *
     * @param info CD信息
     * @return CD值
     */
    private long getCooldownInactive(SkillCooldownInfo info)
    {
        return (info.getLastInvoke() - plugin.getCurrentTick()) + info.getCooldown();
    }

    /**
     * 切换某个玩家当前需要Tick的CD
     *
     * @param uuid 玩家UUID
     * @param info 技能信息
     */
    public void switchCooldown(UUID uuid, @Nullable SkillCooldownInfo info)
    {
        if (info != null && getCooldownInfo(uuid, info.getIdentifier()) != info)
            throw new IllegalArgumentException("传入的Info不属于此玩家");

        if (info == null)
        {
            uuidCooldownMap.remove(uuid);
        }
        else
        {
            if (info.skillInvokedOnce())
                info.setCooldown(this.getCooldownInactive(info));

            uuidCooldownMap.put(uuid, info);
        }
    }

    /**
     * 某个实体类型是否有技能
     *
     * @param id 实体ID
     * @return 是否拥有技能
     */
    public boolean hasSkill(String id)
    {
        var entry = getSkillEntry(id);
        return entry != null
                && !SkillType.UNKNOWN.equals(entry.getKey().getSkillIdentifier())
                && !SkillType.NONE.equals(entry.getKey().getSkillIdentifier());
    }

    /**
     * 某个实体类型是否拥有某个特定的技能
     * @param id 实体ID
     * @param skillKey 目标技能的Key
     * @return 是否拥有
     */
    public boolean hasSpeficSkill(String id, NamespacedKey skillKey)
    {
        var entry = getSkillEntry(id);

        if (entry == null || SkillType.UNKNOWN.equals(entry.getKey().getSkillIdentifier())) return false;

        return entry.getValue().getIdentifier().equals(skillKey);
    }

    /**
     * 获取上次使用技能的时间
     *
     * @param player 目标玩家
     * @return 上次调用时间
     */
    public long getLastInvoke(Player player)
    {
        var info = uuidCooldownMap.getOrDefault(player.getUniqueId(), null);

        return info == null ? Long.MIN_VALUE : info.getLastInvoke();
    }

    /**
     * 清除某个玩家CD列表中不再需要的CD信息
     *
     * @param player 目标玩家
     */
    public void removeUnusedList(Player player)
    {
        var uuid = player.getUniqueId();
        var list = uuidInfoMap.get(uuid);

        if (list == null) return;

        var state = manager.getDisguiseStateFor(player);

        //获取当前CD
        SkillCooldownInfo cdInfo = state == null
                ? null
                : getCooldownInfo(uuid, state.getSkillIdentifier());

        //移除不需要的CD
        list.removeIf(i -> i != cdInfo && this.getCooldownInactive(i) <= 2);

        if (list.isEmpty()) uuidInfoMap.remove(uuid);
    }
}
