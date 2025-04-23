package xyz.nifeather.morph.misc.mobs.ai;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.misc.DisguiseState;

public abstract class MorphBasicAvoidPlayerGoal extends AvoidEntityGoal<Player>
{
    private final MorphManager morphs;
    public final PathfinderMob bindingMob;
    public final float detectDistance;
    public final double walkSpeed;
    public final double sprintSpeed;
    private final RevealingHandler revealingHandler;

    private boolean canBeUsedForRecover;
    public boolean canBeUsedForRecover()
    {
        return this.canBeUsedForRecover;
    }

    public void canBeUsedForRecover(boolean val)
    {
        this.canBeUsedForRecover = val;
    }

    public MorphBasicAvoidPlayerGoal(MorphManager morphs,
                                 RevealingHandler revealingHandler,
                                 PathfinderMob bindingMob,
                                 float detectDistance,
                                 double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, Player.class, detectDistance, walkSpeed, sprintSpeed);
        this.morphs = morphs;
        this.revealingHandler = revealingHandler;
        this.bindingMob = bindingMob;
        this.detectDistance = detectDistance;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
    }

    /**
     * @return 是否逃跑
     */
    @Override
    public boolean canUse()
    {
        this.toAvoid = this.findEntityToAvoid();

        return this.toAvoid != null;
    }

    @Override
    public void start()
    {
        if (this.toAvoid == null)
            return;

        if (findAvoidPath(this.toAvoid))
            this.pathNav.moveTo(this.path, this.walkSpeed);
    }

    /**
     * @return Whether success
     */
    protected boolean findAvoidPath(Player playerToAvoid)
    {
        var targetPosition = DefaultRandomPos.getPosAway(this.mob, 16, 7, playerToAvoid.position());

        if (targetPosition == null)
            return false;

        // 如果目标位置离要逃离的目标更近，那么不要去那里
        if (playerToAvoid.distanceToSqr(targetPosition) < playerToAvoid.distanceToSqr(this.mob))
            return false;

        this.path = this.pathNav.createPath(targetPosition.x, targetPosition.y, targetPosition.z, 0);
        return this.path != null;
    }

    protected Player findEntityToAvoid()
    {
        var entities = mob.level()
                .getEntitiesOfClass(avoidClass, mob.getBoundingBox().inflate(maxDist, 3.0, maxDist), living -> true);

        Player entityFound = null;
        double currentDistance = Double.MAX_VALUE;
        for (Player entity : entities)
        {
            if (!EntitySelector.NO_SPECTATORS.test(entity))
                continue;

            var distance = entity.distanceToSqr(mob);
            if (distance > currentDistance)
                continue;

            var bukkitPlayer = (org.bukkit.entity.Player) entity.getBukkitEntity();

            if (revealingHandler.shouldMobsAwareRevealed(bukkitPlayer))
                continue;

            entityFound = entity;
        }

        if (entityFound == null)
            return null;

        if (!(entityFound.getBukkitEntityRaw() instanceof org.bukkit.entity.Player bukkitPlayer))
            return null;

        var state = morphs.getDisguiseStateFor(bukkitPlayer);

        if (state == null) return null;

        if (panicFrom(entityFound, state))
            return entityFound;

        return null;
    }

    protected abstract boolean panicFrom(Player nmsPlayer, DisguiseState disguiseState);

    @Nullable
    public abstract AvoidEntityGoal<Player> getRecoverGoalOrNull();

    @NotNull
    public static MorphBasicAvoidPlayerGoal findGoalForEntity(PathfinderMob entity,
                                                     @NotNull MorphManager morphManager,
                                                     @NotNull RevealingHandler revealingHandler,
                                                     float detectDistance,
                                                     float walkSpeed,
                                                     float sprintSpeed)
    {
        if (morphManager == null || revealingHandler == null)
            throw new NullDependencyException("Null MorphManager/RevealingHandler for MorphBasicAvoidPlayerGoal?!");

        return switch (entity)
        {
            case Panda panda -> new MorphPandaAvoidPlayerGoal(
                    morphManager, revealingHandler, panda, detectDistance, walkSpeed, sprintSpeed
            );

            case Ocelot ocelot -> new MorphOcelotAvoidEntityGoal(
                    morphManager, revealingHandler, ocelot, detectDistance, walkSpeed, sprintSpeed
            );

            case Rabbit rabbit -> new MorphRabbitAvoidPlayerGoal(
                    morphManager, revealingHandler, rabbit, detectDistance, walkSpeed, sprintSpeed
            );

            case Cat cat -> new MorphCatAvoidPlayerGoal(
                    morphManager, revealingHandler, cat, detectDistance, walkSpeed, sprintSpeed
            );

            default -> new MorphCommonAvoidPlayerGoal(
                    morphManager, revealingHandler, entity, detectDistance, walkSpeed, sprintSpeed
            );
        };
    }
}
