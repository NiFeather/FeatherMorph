package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Objects;
import java.util.Optional;

public class MorphPandaAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Panda>
{
    public MorphPandaAvoidPlayerGoal(Panda bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public GoalKey<@NotNull Panda> getKey()
    {
        return GoalKey.of(Panda.class, Objects.requireNonNull(NamespacedKey.fromString("feathermorph:panda_avoid_player_goal")));
    }

    @Override
    public boolean shouldActivate()
    {
        if (mob.getCombinedGene() != Panda.Gene.WORRIED) return false;

        // In flavor or NMS Panda#canPerformAction
        if (mob.isOnBack() || mob.isScared() || mob.isEating() || mob.isRolling() || mob.isSitting())
            return false;

        return super.shouldActivate();
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
    }

    public static final MorphPandaAvoidPlayerGoal.PandaGoalProvider GOAL_PROVIDER = new MorphPandaAvoidPlayerGoal.PandaGoalProvider();

    public static class PandaGoalProvider implements IGoalProvider<Panda>
    {
        @Override
        public Optional<Panda> tryCast(Entity entity)
        {
            return Optional.ofNullable(entity instanceof Panda Panda ? Panda : null);
        }

        @Override
        public Goal<Panda> createGoal(Panda mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphPandaAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
