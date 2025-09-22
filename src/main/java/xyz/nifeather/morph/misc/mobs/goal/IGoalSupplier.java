package xyz.nifeather.morph.misc.mobs.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

@FunctionalInterface
public interface IGoalSupplier<M extends Mob>
{
    Goal createGoal(M mob,
                    @NotNull MorphManager morphManager,
                    @NotNull RevealingHandler revealingHandler,
                    double detectDistance,
                    double walkSpeed,
                    double sprintSpeed);
}
