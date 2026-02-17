package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.GoalKey;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Objects;
import java.util.Optional;

public class MorphOcelotAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Ocelot>
{
    public MorphOcelotAvoidPlayerGoal(Ocelot bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean canUse()
    {
        return !mob.isTrusting() && super.canUse();
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
    }

    public static final MorphOcelotAvoidPlayerGoal.OcelotGoalProvider GOAL_PROVIDER = new MorphOcelotAvoidPlayerGoal.OcelotGoalProvider();

    public static class OcelotGoalProvider implements IGoalProvider<Ocelot>
    {
        @Override
        public Optional<Ocelot> tryCast(Entity entity)
        {
            return Optional.ofNullable(entity instanceof Ocelot Ocelot ? Ocelot : null);
        }

        @Override
        public Goal createGoal(Ocelot mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphOcelotAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
