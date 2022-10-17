package xiamomc.morph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public void executeDisguiseSkill(Player player)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        var skill = typeSkillMap.get(state.getDisguise().getType().getEntityType());

        if (skill != null)
        {
            state.setAbilityCooldown(skill.executeSkill(player));

            lastSkillTickMap.put(player.getUniqueId(), Plugin.getCurrentTick());
        }
        else
        {
            state.setAbilityCooldown(20);

            player.sendMessage(MessageUtils.prefixes(player, SkillStrings.skillNotAvaliableString()));

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));
        }
    }

    public boolean hasSkill(EntityType type)
    {
        return typeSkillMap.containsKey(type);
    }

    private final Map<UUID, Long> lastSkillTickMap = new ConcurrentHashMap<>();

    /**
     * 获取玩家上次使用主动技能的时间
     * @param player 目标玩家
     * @return 上次使用主动技能的时间，如果没找到则返回0
     */
    public long getLastSkillTick(Player player)
    {
        return lastSkillTickMap.getOrDefault(player.getUniqueId(), 0L);
    }
}
