package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
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
    protected Collection<WrappedGoal> filterGoals(Ocelot mob)
    {
        return goalSelector(mob).getAvailableGoals()
                .stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal().getClass().getSimpleName().equals("OcelotAvoidEntityGoal"))
                .toList();
    }

    @Override
    protected void onTargetGoalFound(Ocelot ocelot, WrappedGoal vanillaGoal)
    {
        var replacingGoal = AvoidPlayerGoals.findGoal(ocelot, morphManager(), revealingHandler(), 16, 0.8, 1.33);
        if (replacingGoal == null) return;

        var selector = goalSelector(ocelot);
        selector.addGoal(vanillaGoal.getPriority(), replacingGoal);
        selector.removeGoal(vanillaGoal);
    }

    @Override
    protected void addDefaultGoals(Ocelot mob)
    {
        var goal = new MorphNearestAttackableGoal(mob, morphManager());
        goalSelector(mob).addGoal(1, goal);
    }
}
