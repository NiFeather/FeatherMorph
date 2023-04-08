package xiamomc.morph.skills.impl;

import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class DelayedMorphSkill<T extends ISkillOption> extends MorphSkill<T>
{
    @Resolved
    private MorphManager manager;

    @Override
    public final int executeSkill(Player player, DisguiseState state, SkillConfiguration configuration, T option)
    {
        if (option == null || configuration == null)
        {
            printErrorMessage(player, configuration + "没有对" + getIdentifier().asString() + "技能进行配置");
            return 10;
        }

        var result = this.preExecute(player, state, configuration, option);

        if (result.success)
            this.addDelayedSkillSchedule(player, () -> executeDelayedSkill(player, state, configuration, option), getExecuteDelay(configuration, option));
        else
            printErrorMessage(player, "执行技能时出现问题");

        return configuration.getCooldown();
    }

    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillConfiguration configuration, T option)
    {
        return ExecuteResult.success(configuration.getCooldown());
    }

    protected abstract int getExecuteDelay(SkillConfiguration configuration, T option);

    protected abstract void executeDelayedSkill(Player player, DisguiseState state, SkillConfiguration configuration, T option);

    protected void addDelayedSkillSchedule(Player player, Runnable execution, int delay)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        if (delay <= 0)
        {
            execution.run();
            return;
        }

        this.addSchedule(() ->
        {
            if (!player.isOnline()) return;

            var currentState = manager.getDisguiseStateFor(player);

            //检查伪装是否为同一个实例（玩家是否更改了伪装）
            if (currentState != null && currentState.getDisguise() == state.getDisguise())
            {
                player.getScheduler().runDelayed(morphPlugin(), r -> execution.run(), null, 1);
            }
        }, delay - 1);
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
