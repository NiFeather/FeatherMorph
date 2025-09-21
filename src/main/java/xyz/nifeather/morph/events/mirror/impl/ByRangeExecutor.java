package xyz.nifeather.morph.events.mirror.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.GameType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.events.PlayerTracker;
import xyz.nifeather.morph.events.mirror.ExecutorHub;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.storage.mirrorlogging.OperationType;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ByRangeExecutor extends ChainedExecutor
{
    public ByRangeExecutor(ExecutorHub executorHub)
    {
        super(executorHub);
    }

    @Override
    protected @Nullable Player findNextControllablePlayerFrom(Player source, List<Player> pendingChain)
    {
        var index = pendingChain.indexOf(source);

        if (index == -1 || index >= pendingChain.size())
            return null;

        return pendingChain.get(index + 1);
    }

    @Override
    protected List<Player> buildSimulateChain(Player source)
    {
        // 获取控制目标
        var targetName = getTargetControlFor(source);
        if (targetName == null) // 没有目标 -> 没有伪装，我们要查找附近伪装为来源的玩家
            targetName = source.getName();

        var controlDistance = executorHub.getControlDistance();
        if (controlDistance == -1)
            controlDistance = 32;

        String finalTargetName = targetName;

        int finalControlDistance = controlDistance;
        Collection<Player> matchedPlayers = null;

        try
        {
            matchedPlayers = FoliaThreadUtils.runOnEntitySync(source, src ->
            {
                return src.getWorld().getNearbyPlayers(src.getLocation(), finalControlDistance, candidatePlayer ->
                {
                    if (Objects.equals(candidatePlayer, src))
                        return false;

                    if (NmsRecord.ofPlayer(candidatePlayer).gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
                        return false;

                    var theirState = morphManager().getDisguiseStateFor(candidatePlayer);

                    if (theirState != null && theirState.getDisguiseIdentifier().equals("player:" + finalTargetName))
                        return true;
                    else
                        return candidatePlayer.getName().equals(finalTargetName) && theirState == null;
                });
            }, FoliaThreadUtils.DEFAULT_WAIT_TIMEOUT);
        }
        catch (ExecutionException | TimeoutException | InterruptedException e)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.error("Failed to build byRange simulate chain", e);
        }

        var list = new ObjectArrayList<Player>();

        list.add(source);
        list.addAll(matchedPlayers);

        return list;
    }

    @Override
    public boolean onSwing(Player source)
    {
        var isInChain = isInChain(source);

        if (isInChain)
            return false;

        var tracker = tracker();

        //若源玩家正在丢出物品，不要处理
        if (tracker.droppingItemThisTick(source))
            return false;

        var lastAction = tracker.getLastInteractAction(source);

        //如果此时玩家没有触发Interaction, 那么默认设置为左键空气
        if (!tracker.interactingThisTick(source))
            lastAction = PlayerTracker.InteractType.LEFT_CLICK_AIR;

        if (lastAction == null) return false;

        //旁观者模式下左键方块不会产生Interact事件，我们得猜这个玩家现在是左键还是右键
        if (source.getGameMode() == GameMode.SPECTATOR)
        {
            if (lastAction.isRightClick())
                lastAction = PlayerTracker.InteractType.LEFT_CLICK_BLOCK;
        }

        PlayerTracker.InteractType finalLastAction = lastAction;

        runIfChainable(source, targetPlayer ->
        {
            simulateOperation(finalLastAction.toBukkitAction(), targetPlayer, source);
            logOperation(source, targetPlayer, finalLastAction.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
        });

        return false;
    }

    @Override
    public boolean onHurtEntity(Player damager, Player hurted)
    {
        return !isInChain(damager) || !isInChain(hurted);
    }

    @Override
    public void reset()
    {
    }
}
