package xyz.nifeather.morph.abilities;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.List;
import java.util.UUID;

public interface IAbility<T extends ISkillAbilityOption> extends Listener
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

    public default void onClientInit(DisguiseState state)
    {
    }

    /**
     * 更新某个玩家的被动技能
     *
     * @param player 目标玩家
     * @param state {@link DisguiseState}
     * @return 操作是否成功
     */
    public boolean handle(Player player, DisguiseState state);

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
    public List<UUID> getAppliedPlayers();

    @NotNull
    public ISkillAbilityOptionHandler<T> optionHandler();
}
