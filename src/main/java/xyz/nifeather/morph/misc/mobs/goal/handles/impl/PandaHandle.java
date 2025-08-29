package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
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
    protected Collection<Goal<@NotNull Panda>> filterGoals(Panda panda)
    {
        return mobGoals().getGoals(panda, VanillaGoal.PANDA_AVOID);
    }

    @Override
    protected void onTargetGoalFound(Panda panda, Goal<@NotNull Panda> vanillaGoal)
    {
        mobGoals().removeGoal(panda, vanillaGoal);

        var replacingGoal = AvoidPlayerGoals.findGoal(panda, morphManager, revealingHandler, 16, 0.8d, 1.33d);
        if (replacingGoal == null) return;

        mobGoals().addGoal(panda, 6, replacingGoal);
    }
}
