package xyz.nifeather.morph.misc.mobs.goal.handles.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.entity.ai.PaperGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.misc.mobs.goal.handles.IEntityGoalHandle;
import xyz.nifeather.morph.utilities.ReflectionUtils;

import java.util.Collection;
import java.util.Optional;

public abstract class BasicEntityHandle<M extends Mob> implements IEntityGoalHandle<M>
{
    protected final MorphManager morphManager;
    protected final RevealingHandler revealingHandler;
    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    public BasicEntityHandle()
    {
        var api = FeatherMorphAPI.instance();
        assert api != null;

        this.morphManager = api.directAccess().morphManager();
        this.revealingHandler = api.directAccess().revealingHandler();
    }

    protected abstract Collection<Goal<@NotNull M>> filterGoals(M mob);
    protected abstract void onTargetGoalFound(M mob, Goal<@NotNull M> vanillaGoal);

    @Override
    public void apply(M mob)
    {
        var matchingGoals = filterGoals(mob);
        if (matchingGoals.isEmpty()) return;

        findAvoidPlayerGoal(matchingGoals).ifPresent(g -> this.onTargetGoalFound(mob, g));
    }

    protected Optional<Goal<@NotNull M>> findAvoidPlayerGoal(Collection<Goal<@NotNull M>> collection)
    {
        // 移除目标是玩家的AvoidGoal
        for (Goal<@NotNull M> g : collection)
        {
            if (!(g instanceof PaperGoal<M> paperWrapped)) continue;
            if (!(paperWrapped.getHandle() instanceof AvoidEntityGoal<?> avoidEntityGoal)) continue;

            var clazz = (Class<?>) ReflectionUtils.getValue(avoidEntityGoal, "avoidClass", Class.class);
            if (clazz.isAssignableFrom(Player.class))
                return Optional.of(g);
        }

        return Optional.empty();
    }

    protected int getGoalPriority(M mob, Goal<@NotNull M> goal)
    {
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
    }

    protected MobGoals mobGoals()
    {
        return Bukkit.getMobGoals();
    }
}
