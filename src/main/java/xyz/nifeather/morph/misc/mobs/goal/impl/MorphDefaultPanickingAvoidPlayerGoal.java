package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Creature;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

import java.util.Objects;

public class MorphDefaultPanickingAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Creature>
{
    public MorphDefaultPanickingAvoidPlayerGoal(Creature bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
    }

    /**
     * A unique key that identifies this type of goal. Plugins should use their own namespace, not the minecraft
     * namespace. Additionally, this key also specifies to what mobs this goal can be applied to
     *
     * @return the goal key
     */
    @Override
    @NotNull
    public GoalKey<@NotNull Creature> getKey()
    {
        return GoalKey.of(Creature.class, Objects.requireNonNull(NamespacedKey.fromString("feathermorph:default_panicking_avoid_player_goal")));
    }
}
