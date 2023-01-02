package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class DelayedMorphSkill<T extends ISkillOption> extends MorphSkill<T>
{
    @Resolved
    private MorphManager manager;

    public void addDelayedSkillSchedule(Player player, Runnable execution, int delay)
    {
        var state = manager.getDisguiseStateFor(player);

        if (state == null) return;

        this.addSchedule(() ->
        {
            if (!player.isOnline()) return;

            var currentState = manager.getDisguiseStateFor(player);

            //检查伪装是否为同一个实例（玩家是否更改了伪装）
            if (currentState != null && currentState.getDisguise() == state.getDisguise())
                execution.run();
        }, delay);
    }
}
