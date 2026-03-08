package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.Pathfinder;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.*;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.utilities.EntityTypeUtils;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.EnumSet;

/**
 * 一定程度上复刻了 NMS 中的 {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 */
public abstract class MorphBasicAvoidPlayerGoal<M extends Mob> extends Goal
{
    protected final M mob;
    protected final RevealingHandler revealingHandler;
    protected final MorphManager morphManager;
    protected final double detectDistance;
    protected final double walkSpeed;
    protected final double sprintSpeed;

    public MorphBasicAvoidPlayerGoal(M bindingMob,
                                     RevealingHandler revealingHandler,
                                     MorphManager morphManager,
                                     double detectDistance,
                                     double walkSpeed, double sprintSpeed)
    {
        this.mob = bindingMob;
        this.revealingHandler = revealingHandler;
        this.morphManager = morphManager;
        this.detectDistance = detectDistance;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;

        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Nullable
    private Entity entityToAvoid;

    @Override
    public boolean canUse()
    {
        entityToAvoid = findEntityToAvoid();
        if (entityToAvoid == null) return false;

        var mobLocation = mob.getLocation();
        var avoidingLocation = entityToAvoid.getLocation();

        if (FoliaThreadUtils.notInSameRegion(mobLocation, avoidingLocation))
            return false;

        this.path = findEscapePath();
        return path != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !mob.getPathfinder().hasPath();
    }

    @Nullable
    private Entity findEntityToAvoid()
    {
        var trackingDistance = (mob.getBoundingBox().getWidthX() / 2d) + detectDistance;
        var nearbyPlayers = mob.getNearbyEntities(trackingDistance, 3, trackingDistance)
                .stream().filter(e -> e instanceof Player && FoliaThreadUtils.isTickThreadFor(e))
                .map(e -> (Player) e)
                .toList();

        Entity found = null;
        double currentDistance = Double.MAX_VALUE;
        var mobLocation = mob.getLocation();

        boolean avoidPlayerByDefault = mobPanicFromPlayerByDefault();

        for (Player player : nearbyPlayers)
        {
            var gamemode = player.getGameMode();
            if (gamemode == GameMode.SPECTATOR || gamemode == GameMode.CREATIVE)
                continue;

            // Since we are in the same region, it should be safe to compare directly...
            var distance = player.getLocation().distance(mobLocation);
            if (distance > currentDistance)
                continue;

            // Check if player's disguise is what this mob panic from.
            // If not, since it's disguised, we should skip this player.
            var state = morphManager.getDisguiseStateFor(player);
            if (state != null)
            {
                // Player already revealed, we should not panic from it anyway.
                if (revealingHandler.shouldMobsAwareRevealed(player))
                    continue;

                if (EntityTypeUtils.panicsFrom(mob.getType(), state.getEntityType()))
                {
                    found = player;
                    break;
                }

                continue;
            }

            if (avoidPlayerByDefault)
            {
                found = player;
                currentDistance = distance;
            }
        }

        return found;
    }

    protected boolean mobPanicFromPlayerByDefault()
    {
        return EntityTypeUtils.panicsFrom(mob.getType(), EntityType.PLAYER);
    }

    @Nullable
    private Pathfinder.PathResult path;

    @Nullable
    private Pathfinder.PathResult findEscapePath()
    {
        if (entityToAvoid == null)
            return null;

        var nmsMob = (PathfinderMob) ((CraftMob) mob).getHandle();
        var playerToAvoid = ((CraftEntity) entityToAvoid).getHandle();
        Vec3 nmsTarget = DefaultRandomPos.getPosAway(nmsMob, 16, 7, playerToAvoid.position());

        if (nmsTarget == null)
            return null;

        var escapeLocation = new Location(mob.getWorld(), nmsTarget.x(), nmsTarget.y(), nmsTarget.z());
        return mob.getPathfinder().findPath(escapeLocation);
    }

    @Override
    public void start()
    {
        if (path != null)
            this.mob.getPathfinder().moveTo(path, sprintSpeed);
        else
            this.mob.getPathfinder().stopPathfinding();
    }

    @Override
    public void tick()
    {
        // I'm just too lazy to check if comparing distance would trigger async catch,
        // so let's not check for entities that's not on the same thread.
        //
        // ⬇️ Let's keep this null check
        if (entityToAvoid == null || path == null || !FoliaThreadUtils.isTickThreadFor(entityToAvoid)) return;

        var mobLocation = mob.getLocation();
        var avoidingLocation = entityToAvoid.getLocation();

        if (FoliaThreadUtils.notInSameRegion(mobLocation, avoidingLocation))
            return;

        var pathfinder = mob.getPathfinder();
        if (mobLocation.distance(avoidingLocation) < 49)
            pathfinder.moveTo(path, sprintSpeed);
        else
            pathfinder.moveTo(path, walkSpeed);
    }

    @Override
    public void stop()
    {
        entityToAvoid = null;
        path = null;
    }
}
