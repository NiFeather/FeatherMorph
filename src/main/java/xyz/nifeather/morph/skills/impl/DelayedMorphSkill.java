package xyz.nifeather.morph.skills.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

public abstract class DelayedMorphSkill<T extends ISkillAbilityOption> extends MorphSkill<T>
{
    @Resolved
    private MorphManager manager;

    @Override
    public final int executeSkill(Player player, DisguiseState state, T option) throws ExecutionErrorException
    {
        if (option == null)
        {
            throw ExecutionErrorException.forMethod("executeSkill")
                    .withMessage("%s does not have a option set".formatted(state.getDisguiseIdentifier()))
                    .create();
        }

        var executeResult = this.preExecute(player, state, option);

        if (executeResult.success())
            this.addDelayedSkillSchedule(player, () -> executeDelayedSkill(player, state, option), getExecuteDelay(option));

        return executeResult.cd();
    }

    protected abstract ExecuteResult preExecute(Player player, DisguiseState state, @NotNull T option) throws ExecutionErrorException;

    protected abstract int getExecuteDelay(T option);

    protected abstract void executeDelayedSkill(Player player, DisguiseState state, T option);

    protected void addDelayedSkillSchedule(Player player, Runnable execution, int delay)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        if (delay <= 0)
        {
            execution.run();
            return;
        }

        this.scheduleOn(player, () ->
        {
            if (!player.isOnline()) return;

            var currentState = manager.getDisguiseStateFor(player);

            //检查伪装是否为同一个实例（玩家是否更改了伪装）
            if (currentState != null && currentState.getDisguiseWrapper().equals(state.getDisguiseWrapper()))
                execution.run();
        }, delay);
    }

    protected record ExecuteResult(boolean success, int cd)
    {
        public static ExecuteResult success(int cd)
        {
            return new ExecuteResult(true, cd);
        }

        public static ExecuteResult fail(int cd)
        {
            return new ExecuteResult(false, cd);
        }
    }
}
