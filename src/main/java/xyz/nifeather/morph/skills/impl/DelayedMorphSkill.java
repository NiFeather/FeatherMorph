package xyz.nifeather.morph.skills.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;

public abstract class DelayedMorphSkill<T extends ISkillAbilityOption> extends MorphSkill<T>
{
    @Resolved
    private MorphManager manager;

    @Override
    public final int executeSkill(Player player, DisguiseState state, SkillAbilityConfigContainer configuration, T option)
    {
        if (option == null || configuration == null)
        {
            logger.error("%s does not have a potion effect set".formatted(state.getDisguiseIdentifier()));
            notifyError(player);
            return 10;
        }

        var result = this.preExecute(player, state, configuration, option);

        if (result.success)
        {
            this.addDelayedSkillSchedule(player, () -> executeDelayedSkill(player, state, configuration, option), getExecuteDelay(configuration, option));
        }
        else
        {
            logger.error("Delayed skill '%s' for '%s' encountered error while performing pre-execute".formatted(configuration.getSkillIdentifier(), state.getDisguiseIdentifier()));
            notifyError(player);
        }

        return configuration.getSkillCooldown();
    }

    protected ExecuteResult preExecute(Player player, DisguiseState state, @NotNull SkillAbilityConfigContainer configuration, @NotNull T option)
    {
        return ExecuteResult.success(configuration.getSkillCooldown());
    }

    protected abstract int getExecuteDelay(SkillAbilityConfigContainer configuration, T option);

    protected abstract void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfigContainer configuration, T option);

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
