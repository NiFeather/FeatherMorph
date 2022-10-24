package xiamomc.morph.skills;

import org.bukkit.entity.Player;
import xiamomc.morph.skills.configurations.SkillConfiguration;

public interface IMorphSkill
{
    /**
     * 执行伪装的主动技能
     * @param player 玩家
     * @return 执行后的冷却长度
     */
    public int executeSkill(Player player, SkillConfiguration configuration);

    /**
     * 获取要应用的技能类型
     * @return 技能类型
     */
    public SkillType getType();
}
