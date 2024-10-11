package xyz.nifeather.morph.abilities;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.skill.ISkillOption;

import java.util.List;
import java.util.UUID;

public interface IMorphAbility<T extends ISkillOption> extends Listener
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
    public default boolean handle(Player player, DisguiseState state)
    {
        return true;
    }

    /**
     * 此被动技能的设定是否合法
     */
    public boolean optionValid();

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

    /**
     * 获取和此被动对应的{@link ISkillOption}
     *
     * @return {@link ISkillOption}
     */
    public T getDefaultOption();

    /**
     * 为某个伪装添加设置{@link ISkillOption}
     *
     * @return 操作是否成功
     */
    public default boolean setOption(@NotNull String disguiseIdentifier, @Nullable T option)
    {
        return false;
    }

    /**
     * @apiNote 内部轮子
     * @return option是否可以cast为目标option，为null则返回true并略过，反之返回setOption的结果
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    public default boolean setOptionGeneric(String disguiseIdentifier, ISkillOption option)
    {
        return false;
    }

    /**
     * 清除此被动的所有设置
     */
    @Deprecated(forRemoval = true)
    public default void clearOptions()
    {
    }
}
