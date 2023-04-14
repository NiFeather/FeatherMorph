package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;

public interface IMorphSkill<T extends ISkillOption>
{
    /**
     * 执行伪装的主动技能
     * @param player 玩家
     * @param state {@link DisguiseState}
     * @param configuration 此技能的整体配置，包括ID、冷却等
     * @param option 此技能的详细设置
     * @return 执行后的冷却长度
     */
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, T option);

    /**
     * Called when this skill gets equipped
     * @param state {@link DisguiseState}
     */
    public default void onInitialEquip(DisguiseState state)
    {
    }

    /**
     * Called when a player's client mod gets initialized
     * @param state {@link DisguiseState}
     */
    public default void onClientinit(DisguiseState state)
    {
    }

    /**
     * Called when this skill gets de-equipped
     * @param state {@link DisguiseState}
     */
    public default void onDeEquip(DisguiseState state)
    {
    }

    /**
     * 内部轮子
     */
    @ApiStatus.Internal
    public default int executeSkillGeneric(Player player, DisguiseState state, SkillAbilityConfiguration config, ISkillOption option)
    {
        T castedOption;

        try
        {
            castedOption = (T) option;
        }
        catch (ClassCastException e)
        {
            player.sendMessage(MessageUtils.prefixes(player, SkillStrings.exceptionOccurredString()));
            return 20;
        }

        return executeSkill(player, state, config, castedOption);
    }

    /**
     * 获取要应用的技能ID
     * @return 技能ID
     */
    @NotNull
    public NamespacedKey getIdentifier();

    /**
     * 获取和此技能对应的{@link ISkillOption}实例
     *
     * @return {@link ISkillOption}
     */
    public T getOption();
}
