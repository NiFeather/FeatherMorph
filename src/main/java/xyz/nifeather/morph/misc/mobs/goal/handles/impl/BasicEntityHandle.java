package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.slf4j.Logger;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.handles.IEntityGoalHandle;
import xyz.nifeather.morph.utilities.ReflectionUtils;

import java.util.Collection;
import java.util.Optional;

public abstract class BasicEntityHandle<M extends Mob> extends MorphPluginObject implements IEntityGoalHandle<M>
{
    @Resolved(shouldSolveImmediately = true)
    private volatile MorphManager morphManager;

    protected MorphManager morphManager()
    {
        return morphManager;
    }

    @Resolved(shouldSolveImmediately = true)
    private volatile RevealingHandler revealingHandler;

    protected RevealingHandler revealingHandler()
    {
        return revealingHandler;
    }

    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    public BasicEntityHandle()
    {
    }

    protected abstract Collection<WrappedGoal> filterGoals(M mob);
    protected abstract void onTargetGoalFound(M mob, WrappedGoal vanillaGoal);

    @Override
    public final void apply(M mob)
    {
        addDefaultGoals(mob);

        var matchingGoals = filterGoals(mob);
        if (matchingGoals.isEmpty()) return;

        findAvoidPlayerGoal(matchingGoals).ifPresentOrElse(g -> this.onTargetGoalFound(mob, g), () -> this.whenNoTargetGoal(mob));
    }

    protected void addDefaultGoals(M mob)
    {
    }

    protected void whenNoTargetGoal(M mob)
    {
    }

    protected Optional<WrappedGoal> findAvoidPlayerGoal(Collection<WrappedGoal> collection)
    {
        // 移除目标是玩家的AvoidGoal
        for (var wrapped : collection)
        {
            var underlyingGoal = wrapped.getGoal();

            var clazz = (Class<?>) ReflectionUtils.getValue(underlyingGoal, "avoidClass", Class.class);
            if (clazz.isAssignableFrom(Player.class))
                return Optional.of(wrapped);
        }

        return Optional.empty();
    }

    @Deprecated
    protected int getGoalPriority(M mob, WrappedGoal goal)
    {
        return goal.getPriority();
        /*
        var goalSelector = ((CraftMob) mob).getHandle().goalSelector;
        var matchedGoal = goalSelector.getAvailableGoals()
                .stream()
                .filter(wrapped -> wrapped.getGoal().asPaperGoal().equals(goal))
                .findFirst();

        if (FeatherMorphMain.getInstance().debugOutputEnabled())
        {
            if (matchedGoal.isEmpty())
                logger.error("The given goal '%s' does not exist for the given mob '%s'".formatted(goal, mob));
            else
                logger.info("Given goal has priority %s for entity %s".formatted(matchedGoal.get().getPriority(), mob));
        }

        return matchedGoal.map(WrappedGoal::getPriority).orElse(0);
        */
    }

    protected GoalSelector goalSelector(M mob)
    {
        return ((CraftMob)mob).getHandle().goalSelector;
    }
}
