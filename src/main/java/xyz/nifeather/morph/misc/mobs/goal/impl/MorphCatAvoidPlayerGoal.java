package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Objects;
import java.util.Optional;

public class MorphCatAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Cat>
{
    public MorphCatAvoidPlayerGoal(Cat bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean shouldActivate()
    {
        return !mob.isTamed() && super.shouldActivate();
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
    }

    @Override
    public GoalKey<@NotNull Cat> getKey()
    {
        return GoalKey.of(Cat.class, Objects.requireNonNull(NamespacedKey.fromString("feathermorph:cat_avoid_player_goal")));
    }

    public static final CatGoalProvider GOAL_PROVIDER = new CatGoalProvider();

    public static class CatGoalProvider implements IGoalProvider<Cat>
    {
        @Override
        public Optional<Cat> tryCast(Entity entity)
        {
            return Optional.ofNullable(entity instanceof Cat cat ? cat : null);
        }

        @Override
        public Goal<Cat> createGoal(Cat mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphCatAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
