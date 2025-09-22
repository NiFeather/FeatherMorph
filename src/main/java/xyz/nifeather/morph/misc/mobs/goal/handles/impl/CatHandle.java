package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;
import xyz.nifeather.morph.misc.mobs.goal.impl.MorphNearestAttackableGoal;

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
    protected Collection<WrappedGoal> filterGoals(Cat mob)
    {
        return goalSelector(mob).getAvailableGoals()
                .stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal().getClass().getSimpleName().equals("CatAvoidEntityGoal"))
                .toList();
    }

    @Override
    protected void onTargetGoalFound(Cat cat, WrappedGoal vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(cat, morphManager(), revealingHandler(), 16, 0.8d, 1.33d);
        if (replacingGoal == null) return;

        var selector = goalSelector(cat);
        selector.removeGoal(vanillaGoal.getGoal());
        selector.addGoal(vanillaGoal.getPriority(), replacingGoal);
    }

    @Override
    protected void addDefaultGoals(Cat mob)
    {
        var goal = new MorphNearestAttackableGoal(mob, morphManager());
        goalSelector(mob).addGoal(1, goal);
    }
}
