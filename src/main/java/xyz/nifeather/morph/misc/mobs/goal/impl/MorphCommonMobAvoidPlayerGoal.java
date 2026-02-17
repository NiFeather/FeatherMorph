package xyz.nifeather.morph.misc.mobs.goal.impl;

import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.IGoalProvider;

import java.util.Objects;
import java.util.Optional;

/**
 * This checks for whether the mob's type would panic from the player, while {@link MorphDefaultPanickingAvoidPlayerGoal} always make the mob panic from the player
 */
public class MorphCommonMobAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Mob>
{
    public MorphCommonMobAvoidPlayerGoal(Mob bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    public static final MorphCommonMobAvoidPlayerGoal.CommonMobGoalProvider GOAL_PROVIDER = new MorphCommonMobAvoidPlayerGoal.CommonMobGoalProvider();

    public static class CommonMobGoalProvider implements IGoalProvider<Mob>
    {
        @Override
        public Optional<Mob> tryCast(Entity entity)
        {
            return Optional.ofNullable(entity instanceof Mob commonMob ? commonMob : null);
        }

        @Override
        public Goal createGoal(Mob mob, @NotNull MorphManager morphManager, @NotNull RevealingHandler revealingHandler, double detectDistance, double walkSpeed, double sprintSpeed)
        {
            return new MorphCommonMobAvoidPlayerGoal(mob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
        }
    }
}
