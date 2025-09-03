package xyz.nifeather.morph.skills;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

public interface ISkill<T extends ISkillAbilityOption>
{
    /**
     * 执行变形形态的主动技能
     *
     * @param player 玩家
     * @param state  {@link DisguiseState}
     * @param option 此技能的详细设置
     * @return The override cooldown time if greater than 0
     * @throws ExecutionErrorException There's an error while executing the skill
     */
    public int executeSkill(Player player, DisguiseState state, T option) throws ExecutionErrorException;

    /**
     * Called when this skill gets equipped
     * @implNote We don't suggest sending data to the client in this method, do that in {@link ISkill#applyToClient(DisguiseState)} instead
     * @param state {@link DisguiseState}
     */
    public default void onInitialEquip(DisguiseState state)
    {
    }

    /**
     * Apply data to the client
     * @param state {@link DisguiseState}
     */
    public default void applyToClient(DisguiseState state)
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
     * 获取要应用的技能ID
     * @return 技能ID
     */
    @NotNull
    public NamespacedKey getIdentifier();

    public ISkillAbilityOptionHandler<T> optionHandler();
}
