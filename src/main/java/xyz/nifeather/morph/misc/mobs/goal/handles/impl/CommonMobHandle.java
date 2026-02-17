package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;
import xyz.nifeather.morph.misc.mobs.goal.impl.MorphDefaultPanickingAvoidPlayerGoal;
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
    protected Collection<WrappedGoal> filterGoals(Creature mob)
    {
        return goalSelector(mob).getAvailableGoals()
                .stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal() instanceof AvoidEntityGoal<?>)
                .toList();
    }

    @Override
    protected void onTargetGoalFound(Creature creature, WrappedGoal vanillaGoal)
    {
        // Dirty fix for wandering traders, we may want to detect entity's walk/sprintSpeed in future
        boolean isWanderingTrader = creature.getType() == EntityType.WANDERING_TRADER;
        double walkSpeed = isWanderingTrader ? 0.5 : 0.8;
        double sprintSpeed = isWanderingTrader ? 0.5 : 1.33;

        var replacingGoal = AvoidPlayerGoals.findGoal(creature, morphManager(), revealingHandler(), 16, walkSpeed, sprintSpeed, this::createDefaultPanickingGoal);
        if (replacingGoal == null) return;

        var selector = goalSelector(creature);
        selector.addGoal(vanillaGoal.getPriority(), replacingGoal);
        selector.removeGoal(vanillaGoal);
    }

    private Goal createDefaultPanickingGoal(Creature mob,
                                            @NotNull MorphManager morphManager,
                                            @NotNull RevealingHandler revealingHandler,
                                            double detectDistance,
                                            double walkSpeed,
                                            double sprintSpeed)
    {
        return new MorphDefaultPanickingAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    protected void whenNoTargetGoal(Creature creature)
    {
        // Dirty fix for wandering traders, we may want to detect entity's walk/sprintSpeed in future
        boolean isWanderingTrader = creature.getType() == EntityType.WANDERING_TRADER;
        double walkSpeed = isWanderingTrader ? 0.5 : 0.8;
        double sprintSpeed = isWanderingTrader ? 0.5 : 1.33;

        var defaultGoal = AvoidPlayerGoals.findGoal(creature, morphManager(), revealingHandler(), 16, walkSpeed, sprintSpeed);
        if (defaultGoal == null) return;

        goalSelector(creature).addGoal(4, defaultGoal);
    }

    @Override
    protected void addDefaultGoals(Creature creature)
    {
        var goal = new MorphNearestAttackableGoal(creature, morphManager());
        goalSelector(creature).addGoal(1, goal);
    }
}
