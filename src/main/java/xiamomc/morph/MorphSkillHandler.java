package xiamomc.morph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;
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

    public void executeDisguiseAbility(Player player)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        var skill = typeSkillMap.get(state.getDisguise().getType().getEntityType());

        if (skill != null)
        {
            state.setAbilityCooldown(skill.executeSkill(player));
        }
        else
        {
            state.setAbilityCooldown(20);

            player.sendMessage(MessageUtils.prefixes(player, Component.translatable("此伪装暂时没有技能")
                    .color(NamedTextColor.RED)));

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));
        }
    }

    public boolean hasSkill(EntityType type)
    {
        return typeSkillMap.containsKey(type);
    }
}
