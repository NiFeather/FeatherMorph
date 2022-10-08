package xiamomc.morph.skills;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface IMorphSkill
{
    /**
     * 执行伪装的主动技能
     * @param player 玩家
     * @return 执行后的冷却长度
     */
    public int executeSkill(Player player);

    /**
     * 获取要应用的实体类型
     * @return 实体类型
     */
    public EntityType getType();
}
