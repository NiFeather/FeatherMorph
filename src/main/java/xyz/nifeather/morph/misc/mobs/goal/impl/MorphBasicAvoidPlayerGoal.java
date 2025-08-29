package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.EnumSet;
import java.util.Objects;

public abstract class MorphBasicAvoidPlayerGoal<M extends Mob> implements Goal<@NotNull M>
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
    }

    @Nullable
    private Entity entityToAvoid;

    @Override
    public boolean shouldActivate()
    {
        entityToAvoid = findEntityToAvoid();
        if (entityToAvoid == null) return false;

        this.targetEscapeLocation = findEscapeLocation();
        return targetEscapeLocation != null;
    }

    @Override
    public boolean shouldStayActive()
    {
        return !mob.getPathfinder().hasPath();
    }

    private Entity findEntityToAvoid()
    {
        var trackingDistance = (mob.getBoundingBox().getWidthX() / 2d) + detectDistance;
        var nearbyPlayers = mob.getNearbyEntities(trackingDistance, 3, trackingDistance)
                .stream().filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .toList();

        Entity found = null;
        double currentDistance = Double.MAX_VALUE;
        var catLocation = mob.getLocation();

        for (Player player : nearbyPlayers)
        {
            var gamemode = player.getGameMode();
            if (gamemode == GameMode.SPECTATOR || gamemode == GameMode.CREATIVE)
                continue;

            var distance = player.getLocation().distance(catLocation);
            if (distance > currentDistance)
                continue;

            if (revealingHandler.shouldMobsAwareRevealed(player))
                continue;

            // 如果玩家有变形，检查玩家的变形形态是否会引起该生物逃跑行为
            // 若没有，则直接选中
            var state = morphManager.getDisguiseStateFor(player);
            if (state != null)
            {
                if (EntityTypeUtils.panicsFrom(mob.getType(), state.getEntityType()))
                    found = player;

                continue;
            }

            found = player;
        }

        return found;
    }

    @Nullable
    private Location targetEscapeLocation;

    @Nullable
    private Location findEscapeLocation()
    {
        if (entityToAvoid == null)
            return null;

        var nmsCat = (PathfinderMob) ((CraftMob) mob).getHandle();
        var playerToAvoid = ((CraftEntity) entityToAvoid).getHandle();
        Vec3 nmsTarget = DefaultRandomPos.getPosAway(nmsCat, 16, 7, playerToAvoid.position());

        if (nmsTarget == null)
            return null;

        return new Location(mob.getWorld(), nmsTarget.x(), nmsTarget.y(), nmsTarget.z());
    }

    @Override
    public void start()
    {
        var path = mob.getPathfinder().findPath(Objects.requireNonNull(targetEscapeLocation));

        if (path == null)
            return;

        this.mob.getPathfinder().moveTo(path, sprintSpeed);
    }

    @Override
    public void stop()
    {
        entityToAvoid = null;
        targetEscapeLocation = null;
    }

    @Override
    @NotNull
    public EnumSet<GoalType> getTypes()
    {
        return EnumSet.of(GoalType.MOVE);
    }
}
