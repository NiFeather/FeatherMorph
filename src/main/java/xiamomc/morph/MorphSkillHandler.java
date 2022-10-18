package xiamomc.morph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MorphSkillHandler extends MorphPluginObject
{
    @Resolved
    private MorphManager manager;

    private final Map<EntityType, IMorphSkill> typeSkillMap = new ConcurrentHashMap<>();

    public void registerSkills(List<IMorphSkill> skills)
    {
        skills.forEach(this::registerSkill);
    }

    public void registerSkill(IMorphSkill skill)
    {
        var type = skill.getType();
        if (!typeSkillMap.containsKey(type)) typeSkillMap.put(type, skill);
        else throw new RuntimeException("已经注册过一个 " + type.getKey() + "的伪装技能了");
    }

    /**
     * 让某个玩家执行伪装技能
     * @param player 目标玩家
     */
    public void executeDisguiseSkill(Player player)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        var skill = typeSkillMap.get(state.getDisguise().getType().getEntityType());

        if (skill != null)
        {
            var cd = getCooldownInfo(player.getUniqueId(), skill);
            cd.setCooldown(skill.executeSkill(player));
            cd.setLastInvoke(Plugin.getCurrentTick());

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
     * 获取某个玩家的CD信息
     * @param uuid 玩家UUID
     * @param skill 技能
     * @return CD信息
     */
    public SkillCooldownInfo getCooldownInfo(UUID uuid, IMorphSkill skill)
    {
        //获取cd列表
        List<SkillCooldownInfo> infos;
        SkillCooldownInfo cdInfo;

        //获取或创建CD列表
        if (!uuidInfoMap.containsKey(uuid)) uuidInfoMap.put(uuid, infos = new ArrayList<>());
        else infos = uuidInfoMap.get(uuid);

        //获取或创建CD
        var cd = infos.stream().filter(i -> i.getSkill().equals(skill)).findFirst();
        if (cd.isEmpty())
        {
            cdInfo = new SkillCooldownInfo(skill);
            infos.add(cdInfo);
        }
        else
            cdInfo = cd.get();

        return cdInfo;
    }

    /**
     * 获取技能冷却
     * @param uuid 玩家UUID
     * @param type 实体类型
     * @return 技能信息，为null则此实体类型没有技能
     */
    @Nullable
    public SkillCooldownInfo getCooldownInfo(UUID uuid, EntityType type)
    {
        var skill = this.typeSkillMap.getOrDefault(type, null);

        if (skill == null) return null;
        else return getCooldownInfo(uuid, skill);
    }

    /**
     * 为不活跃的CD信息计算当前刻的CD值
     * @param info CD信息
     * @return CD值
     */
    private int getCooldownInactive(SkillCooldownInfo info)
    {
        return (int) (info.getLastInvoke() - Plugin.getCurrentTick()) + info.getCooldown();
    }

    /**
     * 切换某个玩家当前需要Tick的CD
     * @param uuid 玩家UUID
     * @param info 技能信息
     */
    public void switchCooldown(UUID uuid, @Nullable SkillCooldownInfo info)
    {
        if (info != null && getCooldownInfo(uuid, info.getSkill()) != info)
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
     * @param type 实体类型
     * @return 是否拥有技能
     */
    public boolean hasSkill(EntityType type)
    {
        return typeSkillMap.containsKey(type);
    }

    /**
     * 获取上次调用
     * @param player 目标玩家
     * @return 上次调用时间
     */
    public long getLastInvoke(Player player)
    {
        var info = uuidCooldownMap.getOrDefault(player.getUniqueId(), null);

        return info == null ? Long.MIN_VALUE : info.getLastInvoke();
    }

    //玩家 -> CD列表
    private final Map<UUID, List<SkillCooldownInfo>> uuidInfoMap = new LinkedHashMap<>();

    //玩家 -> 当前CD
    private final Map<UUID, SkillCooldownInfo> uuidCooldownMap = new LinkedHashMap<>();

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
     * 清除某个玩家CD列表中不再需要的CD信息
     * @param player 目标玩家
     */
    public void removeUnusedList(Player player)
    {
        var uuid = player.getUniqueId();
        var list = uuidInfoMap.get(uuid);
        SkillCooldownInfo cdInfo = null;

        if (list == null) return;

        var state = manager.getDisguiseStateFor(player);
        if (state != null)
            cdInfo = getCooldownInfo(uuid, state.getDisguise().getType().getEntityType());

        //移除不需要的CD
        list.removeIf(i -> this.getCooldownInactive(i) <= 2);
        uuidCooldownMap.remove(uuid);

        if (cdInfo != null)
            list.add(cdInfo);

        if (list.isEmpty()) uuidInfoMap.remove(uuid);
    }
}
