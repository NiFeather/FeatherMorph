package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.AvoidPlayerGoals;
import xyz.nifeather.morph.misc.mobs.goal.impl.MorphDefaultPanickingPanicFromPlayerGoal;
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
        // Dirty fix for wandering traders, we may want to detect entity's walk/sprintSpeed in future
        boolean isWanderingTrader = creature.getType() == EntityType.WANDERING_TRADER;
        double walkSpeed = isWanderingTrader ? 0.5 : 0.8;
        double sprintSpeed = isWanderingTrader ? 0.5 : 1.33;

        var replacingGoal = AvoidPlayerGoals.findGoal(creature, morphManager, revealingHandler, 16, walkSpeed, sprintSpeed, this::createDefaultPanickingGoal);
        if (replacingGoal == null) return;

        mobGoals().addGoal(creature, getGoalPriority(creature, vanillaGoal), replacingGoal);
        mobGoals().removeGoal(creature, vanillaGoal);
    }

    private Goal<@NotNull Creature> createDefaultPanickingGoal(Creature mob,
                                                          @NotNull MorphManager morphManager,
                                                          @NotNull RevealingHandler revealingHandler,
                                                          double detectDistance,
                                                          double walkSpeed,
                                                          double sprintSpeed)
    {
        return new MorphDefaultPanickingPanicFromPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    protected void whenNoTargetGoal(Creature creature)
    {
        // Dirty fix for wandering traders, we may want to detect entity's walk/sprintSpeed in future
        boolean isWanderingTrader = creature.getType() == EntityType.WANDERING_TRADER;
        double walkSpeed = isWanderingTrader ? 0.5 : 0.8;
        double sprintSpeed = isWanderingTrader ? 0.5 : 1.33;

        var defaultGoal = AvoidPlayerGoals.findGoal(creature, morphManager, revealingHandler, 16, walkSpeed, sprintSpeed);
        if (defaultGoal == null) return;

        mobGoals().addGoal(creature, 4, defaultGoal);
    }

    @Override
    protected void addDefaultGoals(Creature creature)
    {
        var goal = new MorphNearestAttackableGoal(creature, morphManager);
        mobGoals().addGoal(creature, 1, goal);
    }
}
