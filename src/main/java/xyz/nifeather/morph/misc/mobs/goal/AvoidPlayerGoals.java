package xyz.nifeather.morph.misc.mobs.goal;

import net.minecraft.world.entity.ai.goal.Goal;
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
import java.util.function.Supplier;

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
    public static <X extends Mob> Goal findGoal(X mob,
                                                @NotNull MorphManager morphManager,
                                                @NotNull RevealingHandler revealingHandler,
                                                double detectDistance,
                                                double walkSpeed,
                                                double sprintSpeed)
    {
        IGoalSupplier<Mob> supplier = MorphCommonMobAvoidPlayerGoal.GOAL_PROVIDER;
        return findGoal(mob, morphManager, revealingHandler, detectDistance, walkSpeed, sprintSpeed, (IGoalSupplier<X>) supplier);
    }

    @Nullable
    public static <X extends Mob> Goal findGoal(X mob,
                                                   @NotNull MorphManager morphManager,
                                                   @NotNull RevealingHandler revealingHandler,
                                                   double detectDistance,
                                                   double walkSpeed,
                                                   double sprintSpeed,
                                                   IGoalSupplier<X> defaultSupplier)
    {
        var matchingProvider = goalProviders.getOrDefault(mob.getType(), null);
        if (matchingProvider == null)
            return defaultSupplier.createGoal(mob, morphManager, revealingHandler, detectDistance, walkSpeed, sprintSpeed);

        IGoalProvider<X> provider = (IGoalProvider<X>) matchingProvider;

        Goal goal = null;
        var optional = provider.tryCast(mob);
        if (optional.isPresent())
            goal = provider.createGoal(optional.get(), morphManager, revealingHandler, detectDistance, walkSpeed, sprintSpeed);

        return goal;
    }
}
