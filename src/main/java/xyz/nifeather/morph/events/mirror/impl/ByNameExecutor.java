package xyz.nifeather.morph.events.mirror.impl;

import ca.spottedleaf.moonrise.common.util.TickThread;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.events.InteractionMirrorProcessor;
import xyz.nifeather.morph.events.PlayerTracker;
import xyz.nifeather.morph.events.mirror.ExecutorHub;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetSneakingCommand;
import xyz.nifeather.morph.storage.mirrorlogging.OperationType;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;
import xyz.nifeather.morph.utilities.ItemUtils;
import xyz.nifeather.morph.utilities.NmsUtils;

import java.util.Objects;
import java.util.function.Consumer;

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
     * @param player The {@link Player} who triggered this operation
     * @return A player who matches the target name
     * @apiNote If {@link ConfigOption#MIRROR_SELECTION_MODE} is set to {@link InteractionMirrorProcessor.InteractionMirrorSelectionMode#BY_SIGHT},
     *          the returned value might be a player who disguised as our searching target.
     */
    @NotNull
    private InteractionMirrorProcessor.PlayerInfo getMirrorTarget(Player player)
    {
        var targetName = getTargetControlFor(player);
        if (targetName == null)
            return new InteractionMirrorProcessor.PlayerInfo(null, InteractionMirrorProcessor.PlayerInfo.notSetStr);

        InteractionMirrorProcessor.PlayerInfo info;

        var targetPlayer = Bukkit.getPlayerExact(targetName);

        if (targetPlayer == null || !playerNotDisguised(targetPlayer))
            info = new InteractionMirrorProcessor.PlayerInfo(null, targetName);
        else
            info = new InteractionMirrorProcessor.PlayerInfo(targetPlayer, targetName);

        return info;
    }

    private void scheduleIfNotInSameRegion(Player targetPlayer, Runnable consumer)
    {
        var nmsPlayer = NmsRecord.ofPlayer(targetPlayer);

        if (!TickThread.isTickThreadFor(nmsPlayer))
            scheduleOn(targetPlayer, consumer);
        else
            consumer.run();
    }

    @Override
    public void onSneak(Player player, boolean sneaking)
    {
        applyToNearByMannequin(player, mannequin -> mannequin.setPose(sneaking ? Pose.SNEAKING : Pose.STANDING));

        var playerInf = getMirrorTarget(player);
        var targetPlayer = playerInf.target();

        if (!playerInDistance(player, playerInf.target()) || targetPlayer.isSneaking() == sneaking) return;

        scheduleIfNotInSameRegion(targetPlayer, () ->
        {
            targetPlayer.setSneaking(sneaking);
            clientHandler().sendCommand(targetPlayer, new S2CSetSneakingCommand(sneaking));

            logOperation(player, targetPlayer, OperationType.ToggleSneak);
        });
    }

    protected void applyToNearByMannequin(Player player, Consumer<Mannequin> consumer)
    {
        if (!player.hasPermission(CommonPermissions.MIRROR_MANNEQUIN))
            return;

        var state = morphManager().getDisguiseStateFor(player);
        if (state == null) return;

        var ourLocation = player.getLocation();
        var distance = executorHub.getControlDistance();

        player.getWorld().getEntitiesByClass(Mannequin.class)
                .stream()
                .filter(m ->
                {
                    boolean success = true;
                    if (distance != -1)
                        success = m.getLocation().distance(ourLocation) <= distance;

                    success = success && this.filterMannequin(m, state);

                    return success;
                })
                .forEach(consumer);
    }

    private boolean filterMannequin(@Nullable Mannequin mannequin, DisguiseState state)
    {
        if (mannequin == null || !FoliaThreadUtils.isTickThreadFor(mannequin))
            return false;

        var entityName = mannequin.customName();
        var disguiseName = state.disguisePropertyHandler().getOr(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty());

        if (entityName == null)
            return disguiseName.equals(Component.empty());

        return entityName.equals(disguiseName);
    }

    @Override
    public void onSwapHand(Player player)
    {
        var playerInf = getMirrorTarget(player);

        if (!playerInDistance(player, playerInf.target()))
            return;

        var targetPlayer = playerInf.target();

        scheduleIfNotInSameRegion(targetPlayer, () ->
        {
            var equipment = targetPlayer.getEquipment();

            var mainHandItem = equipment.getItemInMainHand();
            var offhandItem = equipment.getItemInOffHand();

            equipment.setItemInMainHand(offhandItem);
            equipment.setItemInOffHand(mainHandItem);

            logOperation(player, targetPlayer, OperationType.SwapHand);
        });
    }

    @Override
    public void onHotbarChange(Player player, int slot)
    {
        var inf = getMirrorTarget(player);

        if (!playerInDistance(player, inf.target()))
            return;

        var targetPlayer = inf.target();

        scheduleIfNotInSameRegion(targetPlayer, () ->
        {
            targetPlayer.getInventory().setHeldItemSlot(slot);
            logOperation(player, targetPlayer, OperationType.HotbarChange);
        });
    }

    @Override
    public void onStopUsingItem(Player player, ItemStack itemStack)
    {
        var inf = getMirrorTarget(player);

        if (!playerInDistance(player, inf.target()))
            return;

        var targetPlayer = inf.target();

        scheduleIfNotInSameRegion(targetPlayer, () ->
        {
            //如果目标玩家正在使用的物品和我们当前释放的物品一样，并且释放的物品拥有使用动画，那么调用releaseUsingItem
            var ourHandItem = itemStack.getType();
            var nmsPlayer = NmsRecord.ofPlayer(targetPlayer);

            if (nmsPlayer.isUsingItem()
                    && ItemUtils.isContinuousUsable(ourHandItem)
                    && nmsPlayer.getUseItem().getBukkitStack().getType() == ourHandItem)
            {
                nmsPlayer.releaseUsingItem();

                logOperation(player, targetPlayer, OperationType.ReleaseUsingItem);
            }
        });
    }

    @Override
    public boolean onHurtEntity(Player damager, Player hurted)
    {
        var inf = getMirrorTarget(damager);

        if (!playerInDistance(damager, inf.target()))
            return false;

        var targetPlayer = inf.target();

        simulateOperationAsync(Action.LEFT_CLICK_AIR, targetPlayer, damager, success -> {});
        logOperation(damager, targetPlayer, OperationType.LeftClick);

        if (TickThread.isTickThreadFor(NmsRecord.ofPlayer(targetPlayer)))
        {
            var damagerLookingAt = damager.getTargetEntity(5);
            var playerLookingAt = targetPlayer.getTargetEntity(5);

            //如果伪装的玩家想攻击的实体和被伪装的玩家一样，模拟左键并取消事件
            if (damagerLookingAt != null && damagerLookingAt.equals(playerLookingAt))
                return true;
        }

        return hurted.equals(targetPlayer);
    }

    @Override
    public boolean onSwing(Player source)
    {
        var inf = getMirrorTarget(source);
        var targetPlayer = inf.target();
        if (targetPlayer == null) return false;

        var playerInDistance = playerInDistance(source, inf.target());

        //取消一定条件下源玩家的挥手动画
        if (targetPlayer.getLocation().getWorld().equals(source.getLocation().getWorld())
                && playerInDistance
                && NmsUtils.isTickThreadFor(targetPlayer)
                && NmsUtils.isTickThreadFor(source)
                && Math.abs(targetPlayer.getLocation().distance(source.getLocation())) <= 6)
        {
            var theirTarget = targetPlayer.getTargetEntity(5);
            var ourTarget = source.getTargetEntity(5);

            if ((ourTarget != null || theirTarget != null)
                    && (Objects.equals(ourTarget, targetPlayer) || Objects.equals(ourTarget, theirTarget) || Objects.equals(theirTarget, source)))
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

        simulateOperationAsync(lastAction.toBukkitAction(), targetPlayer, source, success -> {});
        logOperation(source, targetPlayer, lastAction.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);

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

        var inf = getMirrorTarget(source);

        if (!playerInDistance(source, inf.target()))
            return;

        var targetPlayer = inf.target();

        simulateOperationAsync(action, targetPlayer, source, success -> {});
        logOperation(source, targetPlayer, action.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
    }
}
