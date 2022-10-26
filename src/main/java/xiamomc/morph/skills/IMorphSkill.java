package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.storage.skill.SkillConfiguration;

public interface IMorphSkill
{
    /**
     * 执行伪装的主动技能
     * @param player 玩家
     * @return 执行后的冷却长度
     */
    public int executeSkill(Player player, SkillConfiguration configuration);

    /**
     * 获取要应用的技能ID
     * @return 技能ID
     */
    @NotNull
    public NamespacedKey getIdentifier();
}
