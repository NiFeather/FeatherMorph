package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RabbitHandle extends BasicEntityHandle<Rabbit>
{
    @Override
    public Optional<Rabbit> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Rabbit rabbit ? rabbit : null);
    }

    @Override
    protected Collection<Goal<@NotNull Rabbit>> filterGoals(Rabbit mob)
    {
        return mobGoals().getGoals(mob, VanillaGoal.RABBIT_AVOID_ENTITY);
    }

    @Override
    protected void onTargetGoalFound(Rabbit mob, Goal<@NotNull Rabbit> vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(mob, morphManager(), revealingHandler(), 16, 2.2, 2.2);
        if (replacingGoal == null) return;

        mobGoals().addGoal(mob, getGoalPriority(mob, vanillaGoal), replacingGoal);
        mobGoals().removeGoal(mob, vanillaGoal);
    }
}
