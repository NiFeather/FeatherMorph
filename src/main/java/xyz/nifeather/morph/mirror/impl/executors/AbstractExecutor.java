package xyz.nifeather.morph.mirror.impl.executors;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.events.PlayerTracker;
import xyz.nifeather.morph.mirror.ExecutorHub;
import xyz.nifeather.morph.mirror.IExecutor;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.storage.mirrorlogging.OperationType;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class AbstractExecutor extends MorphPluginObject implements IExecutor<Player, ItemStack, Action>
{
    protected final ExecutorHub executorHub;

    public AbstractExecutor(ExecutorHub executorHub)
    {
        this.executorHub = executorHub;
    }

    protected void logOperation(Player source, LivingEntity targetPlayer, OperationType type)
    {
        executorHub.logOperation(source, targetPlayer, type);
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @Resolved
    private PlayerTracker tracker;

    protected PlayerTracker tracker()
    {
        return tracker;
    }

    protected MorphClientHandler clientHandler()
    {
        return clientHandler;
    }

    protected MorphManager morphManager()
    {
        return manager;
    }

    protected boolean playerNotDisguised(Player player)
    {
        //ignoreDisguised.get() ? false : !isDisguised;
        return manager.getDisguiseStateFor(player) == null;
    }

    @Contract("_, null-> false; _, !null -> _")
    protected <E extends LivingEntity> boolean playerInDistance(@NotNull Player source, @Nullable E target)
    {
        if (target == null)
            return false;

        var handle = executorHub.lookupOperationHandle(target);

        if (!handle.operationAllowed(source) || !handle.affectedByMirror(target))
            return false;

        var isInSameWorld = target.getWorld().equals(source.getWorld());
        var normalDistance = executorHub.getControlDistance();

        //normalDistance为-1，总是启用，为0则禁用
        return normalDistance == -1
                || (normalDistance != 0 && isInSameWorld && target.getLocation().distance(source.getLocation()) <= normalDistance);
    }

    protected <E extends LivingEntity> void simulateOperationAsync(Action action, E target, Player source, Consumer<Boolean> callback)
    {
        AtomicBoolean success = new AtomicBoolean(false);
        target.getScheduler().run(plugin, task ->
        {
            success.set(simulateOperation(action, target, source));
            callback.accept(success.get());
        }, () -> { /* retired */ });
    }

    /**
     * 模拟玩家操作
     *
     * @param action 操作类型
     * @param target 目标玩家
     * @return 操作是否成功
     */
    protected <E extends LivingEntity> boolean simulateOperation(Action action, E target, Player source)
    {
        // 如果栈内包含目标玩家，或者此玩家这个tick已经和环境互动过了一次，那么忽略此操作
        if (target instanceof Player targetPlayer && tracker().interactingThisTick(targetPlayer))
            return false;

        var isRightClick = action.isRightClick();
        var simulator = executorHub.lookupOperationHandle(target);
        var result = isRightClick
                ? simulator.simulateRightClick(target)
                : simulator.simulateLeftClick(target);

        boolean success = false;

        if (result.success())
        {
            var equipment = target.getEquipment();
            Material itemInUse = equipment == null ? Material.AIR : target.getEquipment().getItem(result.hand()).getType();

            if (!isRightClick || !ItemUtils.isContinuousUsable(itemInUse) || result.forceSwing())
            {
                var allowed = !(target instanceof Player player) || new PlayerArmSwingEvent(player, result.hand()).callEvent();

                if (allowed)
                    target.swingHand(result.hand());
            }

            success = true;
        }

        return success;
    }
}
