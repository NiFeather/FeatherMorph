package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;
import xyz.nifeather.morph.misc.mobs.goal.impl.MorphNearestAttackableGoal;

import java.util.Collection;
import java.util.Optional;

public class CommonMobHandle extends BasicEntityHandle<Creature>
{
    @Override
    public Optional<Creature> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Creature creature ? creature : null);
    }

    @Override
    protected Collection<Goal<@NotNull Creature>> filterGoals(Creature mob)
    {
        return mobGoals().getGoals(mob, VanillaGoal.AVOID_ENTITY);
    }

    @Override
    protected void onTargetGoalFound(Creature creature, Goal<@NotNull Creature> vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(creature, morphManager, revealingHandler, 16, 0.8, 1.33);
        if (replacingGoal == null) return;

        mobGoals().addGoal(creature, getGoalPriority(creature, vanillaGoal), replacingGoal);
        mobGoals().removeGoal(creature, vanillaGoal);
    }

    @Override
    protected void addDefaultGoals(Creature creature)
    {
        var goal = new MorphNearestAttackableGoal(creature, morphManager);
        mobGoals().addGoal(creature, 1, goal);
    }

    @Override
    protected void whenNoTargetGoal(Creature creature)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(creature, morphManager, revealingHandler, 16, 0.8, 1.33);
        if (replacingGoal == null) return;

        mobGoals().addGoal(creature, 4, replacingGoal);
    }
}
