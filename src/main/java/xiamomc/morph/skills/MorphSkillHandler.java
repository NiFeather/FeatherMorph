package xiamomc.morph.skills;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.events.api.gameplay.PlayerExecuteSkillEvent;
import xiamomc.morph.events.api.lifecycle.SkillsFinishedInitializeEvent;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.skills.impl.*;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfigurationStore;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphSkillHandler extends MorphPluginObject
{
    /**
     * 已注册的技能
     */
    private final List<IMorphSkill<?>> skills = new ObjectArrayList<>();

    /**
     * 获取已注册的技能
     *
     * @return 技能列表
     */
    public List<IMorphSkill<?>> getRegistedSkills()
    {
        return skills;
    }

    /**
     * 玩家 -> 此玩家的CD列表
     */
    private final Map<UUID, List<SkillCooldownInfo>> uuidInfoMap = new Object2ObjectOpenHashMap<>();

    /**
     * 玩家 -> 当前CD
     */
    private final Map<UUID, SkillCooldownInfo> activeCooldownMap = new Object2ObjectOpenHashMap<>();

    @Resolved
    private MorphManager manager;

    @Resolved
    private SkillAbilityConfigurationStore store;

    @Initializer
    private void load()
    {
        registerSkills(ObjectList.of(
                new ApplyEffectMorphSkill(),
                new ExplodeMorphSkill(),
                new InventoryMorphSkill(),
                new LaunchProjectiveMorphSkill(),
                new SummonFangsMorphSkill(),
                new TeleportMorphSkill(),
                new SonicBoomMorphSkill(),
                new SplashPotionSkill(),

                NoneMorphSkill.instance
        ));

        this.addSchedule(this::update);

        Bukkit.getPluginManager().callEvent(new SkillsFinishedInitializeEvent(this));
    }

    /**
     * 注册一批技能
     * @param skills 技能列表
     * @return 所有操作是否成功
     */
    public boolean registerSkills(List<IMorphSkill<?>> skills)
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
    public boolean registerSkill(IMorphSkill<?> skill)
    {
        //logger.info("Registering skill: " + skill.getIdentifier().asString());

        if (skills.contains(skill))
        {
            logger.error("Can't register skill: Another skill instance has already registered as " + skill.getIdentifier().asString() + " !");
            return false;
        }

        if (skill.getIdentifier().equals(SkillType.UNKNOWN))
        {
            logger.error("Can't register skill: Illegal skill identifier: " + SkillType.UNKNOWN);
            return false;
        }

        skills.add(skill);

        return true;
    }

    private void update()
    {
        this.addSchedule(this::update);

        //更新CD
        if (!activeCooldownMap.isEmpty())
            activeCooldownMap.forEach((u, c) -> c.setCooldown(c.getCooldown() - 1));
    }

    /**
     * 获取某个ID对应的技能和技能配置
     * @param identifier ID
     * @return 对应的技能和技能配置，如果没找到则是null
     */
    @Nullable
    private Map.Entry<SkillAbilityConfiguration, IMorphSkill<?>> getSkillEntry(String identifier)
    {
        if (identifier == null) return null;

        return store.getConfiguredSkills().entrySet().stream()
                .filter(d -> identifier.equals(d.getKey().getIdentifier())).findFirst().orElse(null);
    }

    /**
     * 获取和identifier匹配的技能
     *
     * @param identifier 技能ID
     * @return {@link IMorphSkill}
     * @apiNote 如果未找到则返回 {@link NoneMorphSkill#instance}
     */
    @NotNull
    public IMorphSkill<?> getSkill(String identifier)
    {
        var entry = getSkillEntry(identifier);

        if (entry != null) return entry.getValue();
        else return NoneMorphSkill.instance;
    }

    public void executeDisguiseSkill(Player player)
    {
        this.executeDisguiseSkill(player, false);
    }

    /**
     * 让某个玩家执行伪装技能
     *
     * @param player 目标玩家
     */
    public void executeDisguiseSkill(Player player, boolean bypassPermission)
    {
        if (!bypassPermission && !player.hasPermission(CommonPermissions.SKILL))
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

            return;
        }

        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        var entry = getSkillEntry(state.getSkillLookupIdentifier());

        if (entry != null && !entry.getKey().getSkillIdentifier().equals(SkillType.NONE) && player.getGameMode() != GameMode.SPECTATOR)
        {
            var cdInfo = getCooldownInfo(player.getUniqueId(), state.getSkillLookupIdentifier());
            assert cdInfo != null;

            if (cdInfo.getCooldown() > 0)
            {
                player.sendMessage(MessageUtils.prefixes(player,
                        SkillStrings.skillPreparing().resolve("time", state.getSkillCooldown() / 20 + "")));
    
                player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                        Sound.Source.PLAYER, 1f, 1f));

                return;
            }

            var event = new PlayerExecuteSkillEvent(player, state);
            event.callEvent();

            if (event.isCancelled())
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.operationCancelledString()));
                state.setSkillCooldown(5);
                return;
            }

            var skill = entry.getValue();
            var config = entry.getKey();

            ISkillOption option;

            try
            {
                option = skill.getOptionInstance().fromMap(config.getSkillOptions(skill));
            }
            catch (Throwable t)
            {
                if (t instanceof ClassCastException)
                    logger.error(config.getIdentifier() + " -> " + skill.getIdentifier() + " has an invalid setting, please check your skill configurations.");
                else
                    logger.error("Error occurred while parsing skill configuration");

                t.printStackTrace();

                player.sendMessage(MessageUtils.prefixes(player, SkillStrings.exceptionOccurredString()));
                return;
            }

            var cd = skill.executeSkillGeneric(player, state, config, option);
            cdInfo.setLastInvoke(plugin.getCurrentTick());

            state.getSoundHandler().resetSoundTime();

            if (!state.haveCooldown()) state.setCooldownInfo(cdInfo);
            else state.setSkillCooldown(cd);
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
     * @param disguiseIdentifier 技能ID
     * @return 技能信息，为null则传入的实体类型是null
     */
    @Nullable
    public SkillCooldownInfo getCooldownInfo(UUID uuid, @Nullable String disguiseIdentifier)
    {
        if (disguiseIdentifier == null) return null;

        //获取cd列表
        List<SkillCooldownInfo> infos;
        SkillCooldownInfo cdInfo;

        //获取或创建CD列表
        if (!uuidInfoMap.containsKey(uuid)) uuidInfoMap.put(uuid, infos = new ObjectArrayList<>());
        else infos = uuidInfoMap.get(uuid);

        //获取或创建CD
        var cd = infos.stream()
                .filter(i -> i.getIdentifier().equals(disguiseIdentifier)).findFirst().orElse(null);

        if (cd == null)
        {
            cdInfo = new SkillCooldownInfo(disguiseIdentifier);
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
            activeCooldownMap.remove(uuid);
        }
        else
        {
            if (info.skillInvokedOnce())
                info.setCooldown(this.getCooldownInactive(info));

            activeCooldownMap.put(uuid, info);
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
        var info = activeCooldownMap.getOrDefault(player.getUniqueId(), null);

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
                : getCooldownInfo(uuid, state.getSkillLookupIdentifier());

        //移除不需要的CD
        list.removeIf(i -> i != cdInfo && this.getCooldownInactive(i) <= 2);

        if (list.isEmpty()) uuidInfoMap.remove(uuid);
    }
}
