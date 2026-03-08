package xyz.nifeather.morph.mirror.impl.executors;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.mirror.ExecutorHub;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.storage.mirrorlogging.OperationType;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ChainedExecutor extends AbstractExecutor
{
    public ChainedExecutor(ExecutorHub executorHub)
    {
        super(executorHub);
    }

    protected final ThreadLocal<List<LivingEntity>> currentSimulateChain = ThreadLocal.withInitial(ArrayList::new);

    protected boolean isInChain(LivingEntity source)
    {
        var list = currentSimulateChain.get();
        return list != null && list.contains(source);
    }

    protected boolean isLastInChain(LivingEntity player)
    {
        var list = currentSimulateChain.get();
        return list != null && (list.indexOf(player) + 1 == list.size());
    }

    protected void runIfChainable(Player source, Consumer<LivingEntity> chainConsumer)
    {
        var currentChain = currentSimulateChain.get();

        if (currentChain == null)
            return;

        if (currentChain.contains(source))
            return;

        // Build the chain and execute
        var chain = buildSimulateChain(source);
        currentSimulateChain.set(chain);

        var first = chain.getFirst();

        chain.forEach(pl ->
        {
            // 跳过第一个（发起调用链）的玩家
            // 这样可能会导致以下情况的发生：
            //
            // 玩家A点击左键 -> 被加入模拟链条 -> 触发A的模拟 -> 继续被加入新的模拟链条
            if (!Objects.equals(pl, first))
                chainConsumer.accept(pl);
        });

        // Cleanup
        currentChain.clear();
        currentSimulateChain.remove();
    }

    /**
     * 寻找给定玩家的下一个可控制目标
     *
     * @param pendingChain 可以用来参考的模拟链，该链可能未完成
     */
    @Nullable
    protected abstract LivingEntity findNextControllableEntityFrom(Player source, List<LivingEntity> pendingChain);

    /**
     * 构建包含发起玩家在内的模拟链
     * @implNote 发起的玩家必须是第一个元素
     *
     * @param source
     * @return
     */
    protected List<LivingEntity> buildSimulateChain(Player source)
    {
        List<LivingEntity> chain = new ObjectArrayList<>();

        chain.add(source);

        Player current = source;
        while (current != null)
        {
            var next = findNextControllableEntityFrom(current, chain);

            // 我们找到了调用链中的玩家！退出以防止死循环
            if (chain.contains(next))
                break;

            if (next != null)
            {
                chain.add(next);

                // Only continue if current entity is player
                if (next instanceof Player nextAsPlayer)
                    current = nextAsPlayer;
                else
                    current = null;
            }
            else
            {
                break;
            }
        }

        return chain;
    }

    //region Controller

    @Override
    public void onSneak(Player source, boolean sneaking)
    {
        this.runIfChainable(source, p ->
        {
            executorHub.lookupOperationHandle(p).simulateSneak(p, sneaking);
            logOperation(source, p, OperationType.ToggleSneak);
        });
    }

    protected boolean filterMannequin(@Nullable Mannequin mannequin, DisguiseState state)
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

    @Override
    public void onSwapHand(Player player)
    {
        this.runIfChainable(player, targetPlayer ->
        {
            executorHub.lookupOperationHandle(targetPlayer).simulateSwap(targetPlayer);
            logOperation(player, targetPlayer, OperationType.SwapHand);
        });
    }

    @Override
    public void onHotbarChange(Player player, int slot)
    {
        this.runIfChainable(player, targetPlayer ->
        {
            executorHub.lookupOperationHandle(targetPlayer).scrollHotbar(targetPlayer, slot);
            logOperation(player, targetPlayer, OperationType.HotbarChange);
        });
    }

    @Override
    public void onStopUsingItem(Player player, ItemStack itemStack)
    {
        this.runIfChainable(player, targetPlayer ->
        {
            executorHub.lookupOperationHandle(targetPlayer).releaseUsingItem(targetPlayer, itemStack);
            logOperation(player, targetPlayer, OperationType.ReleaseUsingItem);
        });
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

        runIfChainable(source, targetPlayer ->
        {
            simulateOperation(action, targetPlayer, source);
            logOperation(source, targetPlayer, action.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
        });
    }

    //endregion Controller
}
