package xiamomc.morph.abilities;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.List;

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
    public List<Player> getAppliedPlayers();

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
    public boolean setOption(@NotNull String disguiseIdentifier, @Nullable T option);

    /**
     * @apiNote 内部轮子
     * @return option是否可以cast为目标option，为null则返回true并略过，反之返回setOption的结果
     */
    @ApiStatus.Internal
    public default boolean setOptionGeneric(String disguiseIdentifier, ISkillOption option)
    {
        T castedOption;

        if (option == null)
            return true;

        try
        {
            castedOption = (T) option;
        }
        catch (ClassCastException e)
        {
            LoggerFactory.getLogger("morph").error("添加设置时出现问题: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return setOption(disguiseIdentifier, castedOption);
    }

    /**
     * 清除此被动的所有设置
     */
    public void clearOptions();
}
