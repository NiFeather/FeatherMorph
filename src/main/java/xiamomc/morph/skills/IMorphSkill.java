package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.SkillConfiguration;

public interface IMorphSkill<T extends ISkillOption>
{
    /**
     * 执行伪装的主动技能
     * @param player 玩家
     * @param configuration 此技能的整体配置，包括ID、冷却等
     * @param option 此技能的详细设置
     * @return 执行后的冷却长度
     */
    public int executeSkill(Player player, SkillConfiguration configuration, T option);

    public default void onInitialEquip(DisguiseState state)
    {
    }

    public default void onClientinit(DisguiseState state)
    {
    }

    public default void onDeEquip(DisguiseState state)
    {
    }

    /**
     * 内部轮子
     */
    @ApiStatus.Internal
    public default int executeSkillGeneric(Player player, SkillConfiguration config, ISkillOption option)
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

        return executeSkill(player, config, castedOption);
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
