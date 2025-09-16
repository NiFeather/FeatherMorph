package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;
import xyz.nifeather.morph.misc.mobs.goal.impl.MorphNearestAttackableGoal;

import java.util.Collection;
import java.util.Optional;

public class OcelotHandle extends BasicEntityHandle<Ocelot>
{
    @Override
    public Optional<Ocelot> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Ocelot ocelot ? ocelot : null);
    }

    @Override
    protected Collection<Goal<@NotNull Ocelot>> filterGoals(Ocelot mob)
    {
        return mobGoals().getGoals(mob, VanillaGoal.OCELOT_AVOID_ENTITY);
    }

    @Override
    protected void onTargetGoalFound(Ocelot ocelot, Goal<@NotNull Ocelot> vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(ocelot, morphManager, revealingHandler, 16, 0.8, 1.33);
        if (replacingGoal == null) return;

        mobGoals().addGoal(ocelot, getGoalPriority(ocelot, vanillaGoal), replacingGoal);
        mobGoals().removeGoal(ocelot, vanillaGoal);
    }

    @Override
    protected void addDefaultGoals(Ocelot mob)
    {
        var goal = new MorphNearestAttackableGoal(mob, morphManager);
        mobGoals().addGoal(mob, 1, goal);
    }
}
