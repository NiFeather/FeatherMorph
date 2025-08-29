package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;

import java.util.Collection;
import java.util.Optional;

public class CatHandle extends BasicEntityHandle<Cat>
{
    @Override
    public Optional<Cat> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Cat cat ? cat : null);
    }

    @Override
    protected Collection<Goal<@NotNull Cat>> filterGoals(Cat mob)
    {
        return mobGoals().getGoals(mob, VanillaGoal.CAT_AVOID_ENTITY);
    }

    @Override
    protected void onTargetGoalFound(Cat cat, Goal<@NotNull Cat> vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(cat, morphManager, revealingHandler, 16, 0.8d, 1.33d);
        if (replacingGoal == null) return;

        mobGoals().removeGoal(cat, vanillaGoal);
        mobGoals().addGoal(cat, 4, replacingGoal);
    }
}
