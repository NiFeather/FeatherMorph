package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;

import java.util.Collection;
import java.util.Optional;

public class PandaHandle extends BasicEntityHandle<Panda>
{
    @Override
    public Optional<Panda> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Panda panda ? panda : null);
    }

    @Override
    protected Collection<WrappedGoal> filterGoals(Panda mob)
    {
        return goalSelector(mob).getAvailableGoals()
                .stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal().getClass().getSimpleName().equals("PandaAvoidGoal"))
                .toList();
    }

    @Override
    protected void onTargetGoalFound(Panda panda, WrappedGoal vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(panda, morphManager(), revealingHandler(), 16, 2, 2);
        if (replacingGoal == null) return;

        var selector = goalSelector(panda);
        selector.addGoal(vanillaGoal.getPriority(), replacingGoal);
        selector.removeGoal(vanillaGoal);
    }
}
