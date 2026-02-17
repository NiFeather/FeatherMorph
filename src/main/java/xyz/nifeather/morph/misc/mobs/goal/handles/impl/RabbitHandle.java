package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;

import java.util.Collection;
import java.util.Optional;

public class RabbitHandle extends BasicEntityHandle<Rabbit>
{
    @Override
    public Optional<Rabbit> tryCast(Entity entity)
    {
        return Optional.ofNullable(entity instanceof Rabbit rabbit ? rabbit : null);
    }

    @Override
    protected Collection<WrappedGoal> filterGoals(Rabbit mob)
    {
        return goalSelector(mob).getAvailableGoals()
                .stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal().getClass().getSimpleName().equals("RabbitAvoidEntityGoal"))
                .toList();
    }

    @Override
    protected void onTargetGoalFound(Rabbit mob, WrappedGoal vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(mob, morphManager(), revealingHandler(), 16, 2.2, 2.2);
        if (replacingGoal == null) return;

        var selector = goalSelector(mob);
        selector.addGoal(vanillaGoal.getPriority(), replacingGoal);
        selector.removeGoal(vanillaGoal);
    }
}
