package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.GoalKey;
import net.minecraft.world.entity.ai.goal.Goal;
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
    public boolean canUse()
    {
        return !mob.isTamed() && super.canUse();
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
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
        public Goal createGoal(Cat mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphCatAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
