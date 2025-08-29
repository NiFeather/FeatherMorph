package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Objects;
import java.util.Optional;

public class MorphRabbitAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Rabbit>
{
    public MorphRabbitAvoidPlayerGoal(Rabbit bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean shouldActivate()
    {
        return mob.getRabbitType() != Rabbit.Type.THE_KILLER_BUNNY && super.shouldActivate();
    }

    @Override
    public GoalKey<@NotNull Rabbit> getKey()
    {
        return GoalKey.of(Rabbit.class, Objects.requireNonNull(NamespacedKey.fromString("feathermorph:rabbit_avoid_player_goal")));
    }

    public static final MorphRabbitAvoidPlayerGoal.RabbitGoalProvider GOAL_PROVIDER = new MorphRabbitAvoidPlayerGoal.RabbitGoalProvider();

    public static class RabbitGoalProvider implements IGoalProvider<Rabbit>
    {
        @Override
        public Optional<Rabbit> tryCast(Entity entity)
        {
            return Optional.ofNullable(entity instanceof Rabbit Rabbit ? Rabbit : null);
        }

        @Override
        public Goal<Rabbit> createGoal(Rabbit mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphRabbitAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
