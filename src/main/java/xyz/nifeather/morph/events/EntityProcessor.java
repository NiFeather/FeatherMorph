package xyz.nifeather.morph.events;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.mobs.ai.FeatherMorphNearestAttackableGoal;
import xyz.nifeather.morph.misc.mobs.ai.MorphBasicAvoidPlayerGoal;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.ReflectionUtils;

public class EntityProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Resolved(shouldSolveImmediately = true)
    private RevealingHandler revealingHandler;

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    private final boolean doModifyAI;
    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    public boolean currentlyDoModifyAI()
    {
        return doModifyAI;
    }

    public EntityProcessor()
    {
        doModifyAI = config.get(Boolean.class, ConfigOption.DO_MODIFY_AI);
        config.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);

        config.getBindable(Boolean.class, ConfigOption.DO_MODIFY_AI).onValueChanged((o, n) ->
        {
            if (doModifyAI == n) return;

            logger.warn("- x - x - x - x - x - x - x - x - x - x - x - x -");
            logger.warn("");
            logger.warn("Changes were made about the option of modifying Mobs' AI.");
            logger.warn("And this requires a server restart!");
            logger.warn("");
            logger.warn("- x - x - x - x - x - x - x - x - x - x - x - x -");

            for (var player : featherMorph().getPlatform().onlinePlayersNative())
            {
                if (player.hasPermission(CommonPermissions.ADMIN))
                {
                    player.sendMessage(MessageUtils.prefixes(player, CommandStrings.aiWarningPrimary()));
                    player.sendMessage(MessageUtils.prefixes(player, CommandStrings.aiWarningSecondary()));
                }
            }
        });
    }

    @EventHandler
    public void onEntityAdded(EntityAddToWorldEvent e)
    {
        if (!doModifyAI) return;

        var entity = e.getEntity();
        if (!(entity instanceof Mob mob)) return;

        var nmsMob = ((CraftMob)mob).getHandle();
        var goalSelector = nmsMob.goalSelector;

        if (!(nmsMob instanceof PathfinderMob pathfinderMob)) return;

        // 添加AvoidEntityGoal
        addAvoidEntityGoal(goalSelector, pathfinderMob);

        // 添加TargetGoal
        nmsMob.goalSelector.addGoal(-1, new FeatherMorphNearestAttackableGoal(manager, nmsMob, Player.class, true, (living, world) -> true));
    }

    private void addAvoidEntityGoal(GoalSelector goalSelector, PathfinderMob sourceMob)
    {
        var availableGoals = goalSelector.getAvailableGoals();

        Goal replacingGoal = null;
        Goal goalFound = null;
        int goalPriority = 0;

        float distance = 16F;
        double slowSpeed = 1D, fastSpeed = 1D;

        // 遍历实体已有的Goal
        for (WrappedGoal wrapped : availableGoals)
        {
            // 跳过不是AvoidEntityGoal的对象
            if (!(wrapped.getGoal() instanceof AvoidEntityGoal<?> avoidEntityGoal)) continue;

            if (wrapped.getGoal() instanceof MorphBasicAvoidPlayerGoal)
            {
                //if (debugOutput.get())
                //    logger.warn("We are processing entity that's already processed?! Found FeatherMorphAvoidPlayerGoal in entity " + sourceMob);

                return;
            }

            // 尝试获取他所要避免的目标类型
            //
            // Class<?>
            // ^
            var fields = ReflectionUtils.getFields(avoidEntityGoal, Class.class, false);
            if (fields.isEmpty()) continue;

            var field = fields.get(0);
            field.setAccessible(true);

            // 创建用于替代它的Goal
            try
            {
                // Class<?>
                //       ^
                var v = field.get(avoidEntityGoal);

                if (v != Player.class) continue;

                // 类型符合，标记移除此Goal
                distance = ReflectionUtils.getValue(avoidEntityGoal, "maxDist", float.class);
                slowSpeed = ReflectionUtils.getValue(avoidEntityGoal, "walkSpeedModifier", double.class);
                fastSpeed = ReflectionUtils.getValue(avoidEntityGoal, "sprintSpeedModifier", double.class);

                goalFound = wrapped;
                goalPriority = wrapped.getPriority();

                break;
            }
            catch (IllegalAccessException e)
            {
                logger.warn("Failed to modify goal", e);
            }
        }

        // 移除并添加我们自己的Goal (如果有找到)
        if (goalFound != null)
            goalSelector.getAvailableGoals().remove(goalFound);

        replacingGoal = MorphBasicAvoidPlayerGoal.findGoalForEntity(
                sourceMob, manager, revealingHandler, distance, (float)slowSpeed, (float)fastSpeed
        );

        goalSelector.addGoal(goalPriority, replacingGoal);
    }
}
