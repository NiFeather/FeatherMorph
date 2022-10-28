package xiamomc.morph.abilities;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.List;

public interface IMorphAbility extends Listener
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @NotNull
    public NamespacedKey getIdentifier();

    /**
     * 应用到玩家
     *
     * @param player 目标玩家
     * @param state {@link DisguiseState}
     * @return 操作是否成功
     */
    public boolean applyToPlayer(Player player, DisguiseState state);

    /**
     * 更新某个玩家的被动技能
     *
     * @param player 目标玩家
     * @param state {@link DisguiseState}
     * @return 操作是否成功
     */
    public default boolean handle(Player player, DisguiseState state)
    {
        return true;
    }

    /**
     * 取消应用某个玩家
     *
     * @param player 目标玩家
     * @param state {@link DisguiseState}
     * @return 操作是否成功
     */
    public boolean revokeFromPlayer(Player player, DisguiseState state);

    /**
     * 获取所有应用了此被动技能的玩家
     *
     * @return 玩家列表
     */
    public List<Player> getAppliedPlayers();
}
