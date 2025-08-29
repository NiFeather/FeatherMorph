package xyz.nifeather.morph.misc.mobs.goal.impl;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.minecraft.world.entity.NeutralMob;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class NewMorphNearestAttackableGoal implements Goal<@NotNull Mob>
{
    private final Mob mob;
    private final MorphManager morphManager;

    public NewMorphNearestAttackableGoal(Mob mob, MorphManager morphManager)
    {
        this.mob = mob;
        this.morphManager = morphManager;
    }

    /**
     * Checks if this goal should be activated
     *
     * @return if this goal should be activated
     */
    @Override
    public boolean shouldActivate()
    {
        if (ThreadLocalRandom.current().nextInt(10) != 0)
            return false;

        var newTarget = findTarget();

        this.targetedEntity = newTarget;
        return newTarget != null;
    }

    @Nullable
    private LivingEntity targetedEntity;

    private double getFollowRange()
    {
        var followRangeAttribute = mob.getAttribute(Attribute.FOLLOW_RANGE);
        return followRangeAttribute == null ? 16 : followRangeAttribute.getValue();
    }

    private LivingEntity findTarget()
    {
        double followRange = getFollowRange();

        var players = mob.getNearbyEntities(followRange, followRange, followRange)
                .stream().filter(e ->
                {
                    if (!(e instanceof Player player)) return false;

                    var gamemode = player.getGameMode();
                    return gamemode == GameMode.SURVIVAL || gamemode == GameMode.ADVENTURE;
                })
                .map(e -> (Player) e)
                .toList();

        for (Player candidate : players)
        {
            var state = morphManager.getDisguiseStateFor(candidate);
            if (state == null) continue;

            if (EntityTypeUtils.hostiles(mob.getType(), state.getEntityType()))
                return candidate;
        }

        return null;
    }

    /**
     * Called when this goal gets activated
     */
    @Override
    public void start()
    {
        mob.setTarget(targetedEntity);
    }

    /**
     * Called when this goal gets stopped
     */
    @Override
    public void stop()
    {
        targetedEntity = null;
    }

    /**
     * Called each tick the goal is activated
     */
    @Override
    public void tick()
    {
        var target = targetedEntity;
        if (target == null) return;

        boolean cancelTarget = false;

        // 当满足以下任一条件时，取消仇恨：
        // 处于不同的世界
        // 目标超过跟随距离
        // 玩家不在线
        // 玩家不是生存模式
        cancelTarget = !mob.getWorld().equals(target.getWorld());
        cancelTarget = cancelTarget || target.getLocation().distance(mob.getLocation()) > getFollowRange();

        if (target instanceof Player targetPlayer)
        {
            var gamemode = targetPlayer.getGameMode();

            cancelTarget = cancelTarget || !targetPlayer.isOnline();
            cancelTarget = cancelTarget || gamemode.isInvulnerable();

            // 如果玩家后来变成了其他会导致恐慌的类型，也取消仇恨
            var state = morphManager.getDisguiseStateFor(targetPlayer);
            if (state != null)
                cancelTarget = cancelTarget || EntityTypeUtils.panicsFrom(mob.getType(), state.getEntityType());
            else
                cancelTarget = true;
        }

        if (!cancelTarget) return;

        if (target.equals(mob.getTarget()))
            mob.setTarget(null);

        var mobHandle = ((CraftMob)mob).getHandle();
        if (mobHandle instanceof NeutralMob neutralMob)
            neutralMob.forgetCurrentTargetAndRefreshUniversalAnger();
    }

    /**
     * A unique key that identifies this type of goal. Plugins should use their own namespace, not the minecraft
     * namespace. Additionally, this key also specifies to what mobs this goal can be applied to
     *
     * @return the goal key
     */
    @Override
    public @NotNull GoalKey<@NotNull Mob> getKey()
    {
        return GoalKey.of(Mob.class, Objects.requireNonNull(NamespacedKey.fromString("feathermorph:nearest_attackable_goal")));
    }

    /**
     * Returns a list of all applicable flags for this goal.<br>
     * <p>
     * This method is only called on construction.
     *
     * @return the subtypes.
     */
    @Override
    public @NotNull EnumSet<GoalType> getTypes()
    {
        return EnumSet.of(GoalType.TARGET);
    }
}
