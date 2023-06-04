package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityTargetEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;

public class EntityTargetingHelper extends MorphPluginObject
{
    private record GoalRecord(Goal goal, int priority, FeatherMorphAvoidPlayerGoal replacingGoal)
    {
        public static GoalRecord of(Goal goal, int priority, FeatherMorphAvoidPlayerGoal replacingGoal)
        {
            return new GoalRecord(goal, priority, replacingGoal);
        }
    }

    private final Map<Mob, GoalRecord> entityGoalMap = new Object2ObjectArrayMap<>();

    /**
     * 为map中的所有实体恢复之前的AvoidGoal
     */
    public void recoverGoal()
    {
        var toRemove = new ObjectArrayList<Mob>();
        entityGoalMap.forEach((mob, goal) ->
        {
            // 跳过已死亡的实体
            if (mob.isDead())
            {
                toRemove.add(mob);
                return;
            }

            var nmsMob = mob instanceof CraftMob craftMob ? craftMob.getHandle() : null;
            if (nmsMob == null) return;

            logger.info("Checking %s" + nmsMob);

            // 如果其目标不为null并且目标未死亡，则跳过
            if (nmsMob.getTarget() != null && !nmsMob.getTarget().isDeadOrDying()) return;

            logger.info("Recovering %s for %s".formatted(goal.goal, nmsMob));
            nmsMob.setTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
            nmsMob.goalSelector.addGoal(goal.priority, goal.goal);
            nmsMob.goalSelector.removeGoal(goal.replacingGoal);
            toRemove.add(mob);
        });

        if (toRemove.size() > 0)
        {
            for (var o : toRemove)
                entityGoalMap.remove(o);
        }
    }

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);
        recoverGoal();
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphs;

    public void entity(List<Entity> entities)
    {
        entities.removeIf(e -> !(e instanceof Mob));
        entities.forEach(e -> this.entitySingle((CraftMob) e));
    }

    /**
     * 为目标生物应用自定义AvoidGoal并寻找目标
     * @param mob 目标生物
     */
    public void entitySingle(Mob mob)
    {
        // 如果实体已有攻击目标，则不做处理
        var nmsMob = mob instanceof CraftMob craftMob ? craftMob.getHandle() : null;

        if (nmsMob == null) return;
        if (nmsMob.getTarget() != null) return;

        var trackingRange = 16;
        var loc = nmsMob.position();
        var nearByPlayers = nmsMob.level.getNearbyPlayers(nmsMob, loc.x, loc.y, loc.z, trackingRange, null);

        // 遍历附近的玩家来寻找目标
        for (var nmsPlayer : nearByPlayers)
        {
            // 跳过非生存玩家
            if (!nmsPlayer.gameMode.isSurvival())
                continue;

            // 跳过没有伪装的玩家
            var state = morphs.getDisguiseStateFor(nmsPlayer.getBukkitEntity());
            if (state == null)
                continue;

            // 如果生物类型和伪装类型不敌对，则跳过
            if (!this.hostiles(mob.getType(), state.getEntityType()))
                continue;

            // 设置攻击目标
            nmsMob.setTarget(nmsPlayer, EntityTargetEvent.TargetReason.CUSTOM, true);

            // 修改Goals已避免AvoidEntityGoal和攻击目标冲突
            var goals = nmsMob.goalSelector;

            // 一些准备
            WrappedGoal goalFound = null;
            FeatherMorphAvoidPlayerGoal replacingGoal = null;
            int priority = 0;

            // 遍历实体已有的Goals
            for (WrappedGoal g : goals.getAvailableGoals())
            {
                // 跳过不是AvoidEntityGoal的对象
                if (!(g.getGoal() instanceof AvoidEntityGoal<?> avoidEntityGoal)) continue;

                // 检查并获取目标类型
                var fields = ReflectionUtils.getFields(avoidEntityGoal, Class.class, false);
                if (fields.size() == 0) continue;

                var field = fields.get(0);
                field.setAccessible(true);

                try
                {
                    var v = field.get(avoidEntityGoal);

                    // 类型符合，标记移除此Goal
                    if (v == Player.class)
                    {
                        replacingGoal = new FeatherMorphAvoidPlayerGoal((PathfinderMob) nmsMob, LivingEntity.class, 16, 1.6D, 1.4D);
                        goalFound = g;
                        priority = g.getPriority();

                        entityGoalMap.put(mob, GoalRecord.of(avoidEntityGoal, g.getPriority(), replacingGoal));
                    }
                }
                catch (Throwable throwable)
                {
                    logger.warn("Failed to modify goal map: " + throwable.getLocalizedMessage());
                    throwable.printStackTrace();
                }
            }

            // 移除并添加我们自己的Goal (如果有找到)
            if (goalFound != null)
                goals.getAvailableGoals().remove(goalFound);

            if (replacingGoal != null)
                goals.addGoal(priority, replacingGoal);

            break;
        }
    }

    private class FeatherMorphAvoidPlayerGoal extends AvoidEntityGoal<LivingEntity>
    {
        public FeatherMorphAvoidPlayerGoal(PathfinderMob mob, Class<LivingEntity> fleeFromType, float distance, double slowSpeed, double fastSpeed)
        {
            super(mob, fleeFromType, distance, slowSpeed, fastSpeed);
        }

        /**
         * @return 是否逃跑
         */
        @Override
        public boolean canUse()
        {
            var superCanUse = super.canUse();
            if (this.toAvoid == null || !(toAvoid.getBukkitEntity() instanceof org.bukkit.entity.Player bukkitPlayer)) return superCanUse;

            var state = EntityTargetingHelper.this.morphs.getDisguiseStateFor(bukkitPlayer);
            return state == null;
        }
    }

    /**
     * 检查源生物和目标生物类型是否敌对
     * @param sourceType 源生物的类型
     * @param targetType 目标生物的类型
     */
    private boolean hostiles(EntityType sourceType, EntityType targetType)
    {
        return switch (sourceType)
        {
            case IRON_GOLEM -> EntityTypeUtils.isEnemy(targetType) && targetType != EntityType.CREEPER;

            case FOX -> targetType == EntityType.CHICKEN || targetType == EntityType.RABBIT
                    || targetType == EntityType.COD || targetType == EntityType.SALMON
                    || targetType == EntityType.TROPICAL_FISH || targetType == EntityType.PUFFERFISH;

            case CAT -> targetType == EntityType.CHICKEN || targetType == EntityType.RABBIT;

            case WOLF -> EntityTypeUtils.isSkeleton(targetType) || targetType == EntityType.RABBIT
                    || targetType == EntityType.LLAMA || targetType == EntityType.SHEEP
                    || targetType == EntityType.FOX;

            case GUARDIAN, ELDER_GUARDIAN -> targetType == EntityType.AXOLOTL || targetType == EntityType.SQUID
                    || targetType == EntityType.GLOW_SQUID;

            // Doesn't work for somehow
            case AXOLOTL -> targetType == EntityType.SQUID || targetType == EntityType.GLOW_SQUID
                    || targetType == EntityType.GUARDIAN || targetType == EntityType.ELDER_GUARDIAN
                    || targetType == EntityType.TADPOLE || targetType == EntityType.DROWNED
                    || targetType == EntityType.COD || targetType == EntityType.SALMON
                    || targetType == EntityType.TROPICAL_FISH || targetType == EntityType.PUFFERFISH;

            default -> false;
        };
    }
}
