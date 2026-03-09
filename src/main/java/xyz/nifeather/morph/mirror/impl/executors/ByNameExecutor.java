package xyz.nifeather.morph.mirror.impl.executors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.events.InteractionMirrorProcessor;
import xyz.nifeather.morph.events.PlayerTracker;
import xyz.nifeather.morph.mirror.ExecutorHub;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.storage.mirrorlogging.OperationType;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.Objects;
import java.util.Optional;

public class ByNameExecutor extends AbstractExecutor
{
    public ByNameExecutor(ExecutorHub executorHub)
    {
        super(executorHub);
    }

    @Override
    public void reset()
    {
    }

    /**
     * Search for a player that matches the target name.
     * @param source The {@link Player} who triggered this operation
     * @return A player who matches the target name
     * @apiNote If {@link xyz.nifeather.morph.config.ConfigOptions#MIRROR_SELECTION_MODE} is set to {@link InteractionMirrorProcessor.InteractionMirrorSelectionMode#BY_SIGHT},
     *          the returned value might be a player who disguised as our searching target.
     */
    private Optional<LivingEntity> getMirrorTarget(Player source)
    {
        var state = morphManager().getDisguiseStateFor(source);
        if (state == null) return Optional.empty();

        if (state.getEntityType() == EntityType.MANNEQUIN)
        {
            var result = source.getWorld().getEntitiesByClass(Mannequin.class)
                    .stream().filter(m -> this.filterMannequin(m, state))
                    .toList()
                    .stream().findFirst().orElse(null); // Return Optional directly don't make IDEA happy somehow

            return Optional.ofNullable(result);
        }
        else
        {
            var targetName = executorHub.getControl(source);
            if (targetName == null)
                return Optional.empty();

            var nullablePlayer = Bukkit.getPlayerExact(targetName);
            if (morphManager().getDisguiseStateFor(nullablePlayer) != null)
                return Optional.empty();
            else
                return Optional.ofNullable(Bukkit.getPlayerExact(targetName));
        }
    }

    private boolean filterMannequin(@Nullable Mannequin mannequin, DisguiseState state)
    {
        if (!FoliaThreadUtils.isTickThreadFor(mannequin))
            return false;

        var entityName = mannequin.customName();
        var entityDescription = mannequin.getDescription();

        var disguiseName = state.disguisePropertyHandler().getOr(PropertyNames.ENTITY_CUSTOM_NAME, null);
        var disguiseDescription = state.disguisePropertyHandler().getOr(PropertyNames.MANNEQUIN_NPC_DESCRIPTION, null);

        if (entityName == null && entityDescription == null)
            return disguiseName == null && disguiseDescription == null;

        return (entityName == null ? disguiseName == null : Objects.equals(entityName, disguiseName))
                && (entityDescription == null ? disguiseDescription == null : Objects.equals(entityDescription, disguiseDescription));
    }

    private void scheduleIfNotInSameRegion(LivingEntity target, Runnable consumer)
    {
        if (!FoliaThreadUtils.isTickThreadFor(target))
            scheduleOn(target, consumer);
        else
            consumer.run();
    }

    @Override
    public void onSneak(Player player, boolean sneaking)
    {
        var targetEntity = getMirrorTarget(player).orElse(null);
        if (targetEntity == null) return;

        boolean targetSneaking = targetEntity.isSneaking() || targetEntity.getPose().equals(Pose.SNEAKING);
        if (!playerInDistance(player, targetEntity) || sneaking == targetSneaking) return;

        scheduleIfNotInSameRegion(targetEntity, () ->
        {
            var simulator = executorHub.lookupOperationHandle(targetEntity);

            simulator.simulateSneak(targetEntity, sneaking);
            logOperation(player, targetEntity, OperationType.ToggleSneak);
        });
    }

    @Override
    public void onSwapHand(Player player)
    {
        var targetEntity = getMirrorTarget(player).orElse(null);
        if (targetEntity == null) return;

        if (!playerInDistance(player, targetEntity))
            return;

        scheduleIfNotInSameRegion(targetEntity, () ->
        {
            executorHub.lookupOperationHandle(targetEntity).simulateSwap(targetEntity);
            logOperation(player, targetEntity, OperationType.SwapHand);
        });
    }

    @Override
    public void onHotbarChange(Player player, int slot)
    {
        var targetEntity = getMirrorTarget(player).orElse(null);
        if (targetEntity == null) return;

        if (!playerInDistance(player, targetEntity))
            return;

        scheduleIfNotInSameRegion(targetEntity, () ->
        {
            executorHub.lookupOperationHandle(targetEntity).scrollHotbar(targetEntity, slot);
            logOperation(player, targetEntity, OperationType.HotbarChange);
        });
    }

    @Override
    public void onStopUsingItem(Player player, ItemStack itemStack)
    {
        var targetEntity = getMirrorTarget(player).orElse(null);
        if (targetEntity == null) return;

        if (!playerInDistance(player, targetEntity))
            return;

        scheduleIfNotInSameRegion(targetEntity, () ->
        {
            executorHub.lookupOperationHandle(targetEntity).releaseUsingItem(targetEntity, itemStack);
            logOperation(player, targetEntity, OperationType.ReleaseUsingItem);
        });
    }

    @Override
    public boolean onHurtEntity(Player damager, LivingEntity hurted)
    {
        var targetEntity = getMirrorTarget(damager).orElse(null);
        if (targetEntity == null) return false;

        if (!playerInDistance(damager, targetEntity))
            return false;

        simulateOperationAsync(Action.LEFT_CLICK_AIR, targetEntity, damager, success -> {});
        logOperation(damager, targetEntity, OperationType.LeftClick);

        if (FoliaThreadUtils.isTickThreadFor(targetEntity))
        {
            var damagerLookingAt = damager.getTargetEntity(5);
            var playerLookingAt = targetEntity.getTargetEntity(5);

            //如果伪装的玩家想攻击的实体和被伪装的玩家一样，模拟左键并取消事件
            if (damagerLookingAt != null && damagerLookingAt.equals(playerLookingAt))
                return true;
        }

        return hurted.equals(targetEntity);
    }

    @Override
    public boolean onSwing(Player source)
    {
        var targetEntity = getMirrorTarget(source).orElse(null);
        if (targetEntity == null) return false;

        var playerInDistance = playerInDistance(source, targetEntity);

        //取消一定条件下源玩家的挥手动画
        if (targetEntity.getLocation().getWorld().equals(source.getLocation().getWorld())
                && playerInDistance
                && FoliaThreadUtils.isTickThreadFor(targetEntity)
                && FoliaThreadUtils.isTickThreadFor(source)
                && Math.abs(targetEntity.getLocation().distance(source.getLocation())) <= 6)
        {
            var theirTarget = targetEntity.getTargetEntity(5);
            var ourTarget = source.getTargetEntity(5);

            if ((ourTarget != null || theirTarget != null)
                    && (Objects.equals(ourTarget, targetEntity) || Objects.equals(ourTarget, theirTarget) || Objects.equals(theirTarget, source)))
            {
                return true;
            }
        }

        if (!playerInDistance)
            return false;

        var tracker = tracker();

        //若源玩家正在丢出物品，不要处理
        //检查玩家在此tick内是否存在互动以避免重复镜像
        if (tracker.droppingItemThisTick(source))
        {
            return false;
        }

        var lastAction = tracker.getLastInteractAction(source);

        //如果此时玩家没有触发Interaction, 那么默认设置为左键空气
        if (!tracker.interactingThisTick(source))
            lastAction = PlayerTracker.InteractType.LEFT_CLICK_AIR;

        if (lastAction == null)
            return false;

        //旁观者模式下左键方块不会产生Interact事件，我们得猜这个玩家现在是左键还是右键
        if (source.getGameMode() == GameMode.SPECTATOR)
        {
            if (lastAction.isRightClick())
                lastAction = PlayerTracker.InteractType.LEFT_CLICK_BLOCK;
        }

        simulateOperationAsync(lastAction.toBukkitAction(), targetEntity, source, success -> {});
        logOperation(source, targetEntity, lastAction.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);

        return false;
    }

    @Override
    public void onInteract(Player source, Action action)
    {
        if (action == Action.PHYSICAL) return;

        //Sometimes right click fires PlayerInteractEvent for both left and right hand.
        //This prevents us from simulating the same operation twice.
        if (tracker().isDuplicatedRightClick(source))
        {
            if (FeatherMorphMain.getInstance().doInternalDebugOutput)
                logger.info("Interact, Duplicated RC " + System.currentTimeMillis());

            return;
        }

        var targetEntity = getMirrorTarget(source).orElse(null);
        if (targetEntity == null) return;

        if (!playerInDistance(source, targetEntity))
            return;

        simulateOperationAsync(action, targetEntity, source, success -> {});
        logOperation(source, targetEntity, action.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
    }
}
