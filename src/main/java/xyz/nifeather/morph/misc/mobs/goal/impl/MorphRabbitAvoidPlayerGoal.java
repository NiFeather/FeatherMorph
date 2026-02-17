package xyz.nifeather.morph.misc.mobs.goal.impl;

import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Optional;

public class MorphRabbitAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Rabbit>
{
    public MorphRabbitAvoidPlayerGoal(Rabbit bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean canUse()
    {
        return mob.getRabbitType() != Rabbit.Type.THE_KILLER_BUNNY && super.canUse();
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
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
        public Goal createGoal(Rabbit mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphRabbitAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
