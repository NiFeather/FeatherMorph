package xyz.nifeather.morph.misc.mobs.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.mobs.goal.impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AvoidPlayerGoals
{
    private static final Map<EntityType, IGoalProvider<?>> goalProviders = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    private static void initGoalMapping()
    {
        register(EntityType.CAT, MorphCatAvoidPlayerGoal.GOAL_PROVIDER);
        register(EntityType.OCELOT, MorphOcelotAvoidPlayerGoal.GOAL_PROVIDER);
        register(EntityType.RABBIT, MorphRabbitAvoidPlayerGoal.GOAL_PROVIDER);
        register(EntityType.PANDA, MorphPandaAvoidPlayerGoal.GOAL_PROVIDER);
    }

    static
    {
        initGoalMapping();
    }

    private static void register(EntityType type, @NotNull IGoalProvider<?> provider)
    {
        goalProviders.put(type, provider);
    }

    @Nullable
    public static <X extends Mob> Goal<@NotNull X> findGoal(X mob,
                                                   @NotNull MorphManager morphManager,
                                                   @NotNull RevealingHandler revealingHandler,
                                                   double detectDistance,
                                                   double walkSpeed,
                                                   double sprintSpeed)
    {
        var matchingProvider = goalProviders.getOrDefault(mob.getType(), null);
        if (matchingProvider == null)
            matchingProvider = MorphCommonMobAvoidPlayerGoal.GOAL_PROVIDER;

        IGoalProvider<X> provider = (IGoalProvider<X>) matchingProvider;

        Goal<@NotNull X> goal = null;
        var optional = provider.tryCast(mob);
        if (optional.isPresent())
            goal = provider.createGoal(optional.get(), morphManager, revealingHandler, detectDistance, walkSpeed, sprintSpeed);

        return goal;
    }
}
