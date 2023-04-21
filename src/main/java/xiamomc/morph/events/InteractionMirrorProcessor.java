package xiamomc.morph.events;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.misc.PlayerOperationSimulator;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.set.S2CSetSneakingCommand;
import xiamomc.morph.storage.mirrorlogging.MirrorSingleEntry;
import xiamomc.morph.storage.mirrorlogging.OperationType;
import xiamomc.morph.utilities.ItemUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionMirrorProcessor extends MorphPluginObject implements Listener
{
    private final Map<Player, String> uuidDisguiseStateMap = new ConcurrentHashMap<>();

    @Resolved
    private MorphClientHandler clientHandler;

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        if (!allowSneak.get()) return;

        var player = e.getPlayer();
        var targetName = uuidDisguiseStateMap.get(player);

        if (targetName != null)
        {
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName, true) || targetPlayer.isSneaking() == e.isSneaking()) return;

            targetPlayer.setSneaking(e.isSneaking());
            clientHandler.sendCommand(targetPlayer, new S2CSetSneakingCommand(e.isSneaking()));

            logOperation(e.getPlayer(), targetPlayer, OperationType.ToggleSneak);
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        if (!allowSwap.get()) return;

        var targetName = uuidDisguiseStateMap.get(e.getPlayer());

        if (targetName != null)
        {
            var player = e.getPlayer();
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName)) return;

            var equipment = targetPlayer.getEquipment();

            var mainHandItem = equipment.getItemInMainHand();
            var offhandItem = equipment.getItemInOffHand();

            equipment.setItemInMainHand(offhandItem);
            equipment.setItemInOffHand(mainHandItem);

            logOperation(player, targetPlayer, OperationType.SwapHand);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e)
    {
        if (!allowDrop.get()) return;

        var player = e.getPlayer();
        var targetName = uuidDisguiseStateMap.get(e.getPlayer());

        if (targetName != null)
        {
            var target = getPlayer(player, targetName);

            if (!playerInDistance(player, target, targetName) || !player.isSneaking()) return;

            if (!target.getEquipment().getItemInMainHand().getType().isAir())
            {
                target.dropItem(false);
                target.swingHand(EquipmentSlot.HAND);

                logOperation(player, target, OperationType.ItemDrop);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent e)
    {
        if (!allowHotBar.get()) return;

        var targetName = uuidDisguiseStateMap.get(e.getPlayer());

        if (targetName != null)
        {
            var player = e.getPlayer();
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName)) return;

            targetPlayer.getInventory().setHeldItemSlot(e.getNewSlot());
            logOperation(player, targetPlayer, OperationType.HotbarChange);
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        uuidDisguiseStateMap.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerStopUsingItem(PlayerStopUsingItemEvent e)
    {
        var player = e.getPlayer();
        var targetName = uuidDisguiseStateMap.get(player);

        if (targetName != null)
        {
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName)) return;

            //如果目标玩家正在使用的物品和我们当前释放的物品一样，并且释放的物品拥有使用动画，那么调用releaseUsingItem
            var ourHandItem = e.getItem().getType();
            var nmsPlayer = NmsRecord.ofPlayer(targetPlayer);

            if (nmsPlayer.isUsingItem()
                    && ItemUtils.isContinuousUsable(ourHandItem)
                    && nmsPlayer.getUseItem().getBukkitStack().getType() == ourHandItem)
            {
                nmsPlayer.releaseUsingItem();
                logOperation(player, targetPlayer, OperationType.ReleaseUsingItem);
            }
        }
    }

    @EventHandler
    public void onPlayerHurtEntity(EntityDamageByEntityEvent e)
    {
        if (!allowSimulation.get()) return;

        if (e.getDamager() instanceof Player damager)
        {
            var targetName = uuidDisguiseStateMap.get(damager);

            if (targetName != null)
            {
                var targetPlayer = getPlayer(damager, targetName);

                if (!playerInDistance(damager, targetPlayer, targetName)) return;

                simulateOperation(Action.LEFT_CLICK_AIR, targetPlayer, damager);
                logOperation(damager, targetPlayer, OperationType.LeftClick);

                //如果伪装的玩家想攻击本体，取消事件并模拟左键
                if (e.getEntity() instanceof Player hurtedPlayer && hurtedPlayer.equals(targetPlayer))
                {
                    e.setCancelled(true);

                    return;
                }

                var damagerLookingAt = damager.getTargetEntity(3);
                var playerLookingAt = targetPlayer.getTargetEntity(3);

                //如果伪装的玩家想攻击的实体和被伪装的玩家一样，模拟左键并取消事件
                if (damagerLookingAt != null && damagerLookingAt.equals(playerLookingAt))
                    e.setCancelled(true);
            }
        }
    }

    @Resolved
    private PlayerTracker tracker;

    @EventHandler
    public synchronized void onPlayerSwing(PlayerArmSwingEvent e)
    {
        if (!allowSimulation.get()) return;

        var player = e.getPlayer();
        var targetName = uuidDisguiseStateMap.get(player);

        if (targetName != null)
        {
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName)) return;

            if (tracker.droppingItemThisTick(player))
                return;

            var lastAction = tracker.getLastInteractAction(player);

            //如果此时玩家没有触发Interaction, 那么默认设置为左键空气
            if (!tracker.interactingThisTick(player))
                lastAction = PlayerTracker.InteractType.LEFT_CLICK_AIR;

            if (lastAction == null) return;

            //旁观者模式下左键方块不会产生Interact事件，我们得猜这个玩家现在是左键还是右键
            if (player.getGameMode() == GameMode.SPECTATOR)
            {
                if (lastAction.isRightClick())
                    lastAction = PlayerTracker.InteractType.LEFT_CLICK_BLOCK;
            }

            //检查玩家在此tick内是否存在互动以避免重复镜像
            if (!tracker.interactingThisTick(player))
            {
                simulateOperation(lastAction.toBukkitAction(), targetPlayer, player);
                logOperation(player, targetPlayer, lastAction.isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
            }
        }
    }

    private final Map<Player, Action> lastRightClick = new Object2ObjectOpenHashMap<>();

    private boolean isDuplicatedRightClick(Player player)
    {
        var lastAction = lastRightClick.getOrDefault(player, null);

        return lastAction != null && lastAction.isRightClick();
    }

    private void updateLastAction(Player player, Action action)
    {
        lastRightClick.put(player, action);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        var player = e.getPlayer();
        var action = e.getAction();

        //Sometimes right click fires PlayerInteractEvent for both left and right hand.
        //This prevents us from simulating the same operation twice.
        if (isDuplicatedRightClick(player))
            return;

        updateLastAction(player, action);

        var targetName = uuidDisguiseStateMap.get(player);

        if (targetName != null)
        {
            var targetPlayer = getPlayer(player, targetName);

            if (!playerInDistance(player, targetPlayer, targetName)) return;

            simulateOperation(e.getAction(), targetPlayer, player);
            logOperation(player, targetPlayer, e.getAction().isLeftClick() ? OperationType.LeftClick : OperationType.RightClick);
        }
    }

    @Resolved
    private PlayerOperationSimulator operationSimulator;

    private final List<Player> ignoredPlayers = new ObjectArrayList<>();

    /**
     * 模拟玩家操作
     *
     * @param action 操作类型
     * @param targetPlayer 目标玩家
     * @return 操作是否成功
     */
    private boolean simulateOperation(Action action, Player targetPlayer, Player source)
    {
        if (!allowSimulation.get()) return false;

        if (ignoredPlayers.contains(source) || ignoredPlayers.contains(targetPlayer) || tracker.interactingThisTick(targetPlayer)) return false;
        ignoredPlayers.add(targetPlayer);
        ignoredPlayers.add(source);

        var isRightClick = action.isRightClick();
        var result = isRightClick
                ? operationSimulator.simulateRightClick(targetPlayer)
                : operationSimulator.simulateLeftClick(targetPlayer);

        if (result.success())
        {
            var itemInUse = targetPlayer.getEquipment().getItem(result.hand()).getType();

            if ((!isRightClick || !ItemUtils.isContinuousUsable(itemInUse)))
                targetPlayer.swingHand(result.hand());

            return true;
        }

        return false;
    }

    public static class InteractionMirrorSelectionMode
    {
        public static final String BY_NAME = "BY_NAME";
        public static final String BY_SIGHT = "BY_SIGHT";
    }

    @Resolved
    private MorphManager manager;

    private Player getPlayer(Player player, String targetName)
    {
        if (selectionMode.get().equalsIgnoreCase(InteractionMirrorSelectionMode.BY_SIGHT))
        {
            var targetEntity = player.getTargetEntity(5);

            if (!(targetEntity instanceof Player targetPlayer)) return null;

            if (targetPlayer.getName().equals(targetName)) return targetPlayer;

            var state = manager.getDisguiseStateFor(targetPlayer);
            if (state != null && state.getDisguiseIdentifier().equals("player:" + targetName))
                return targetPlayer;

            return null;
        }
        else
        {
            return Bukkit.getPlayerExact(targetName);
        }
    }

    private boolean match(Player targetPlayer, String targetName)
    {
        if (ignoreDisguised.get()) return true;

        var backend = manager.getCurrentBackend();
        var disguise = backend.getDisguise(targetPlayer);

        if (disguise != null && disguise.isPlayerDisguise())
            return disguise.getDisguiseName().equals(targetName);

        return disguise == null && targetPlayer.getName().equals(targetName);
    }

    @Contract("_, null, _-> false; _, !null, _ -> _")
    private boolean playerInDistance(@NotNull Player source, @Nullable Player target, String targetName)
    {
        return playerInDistance(source, target, targetName, false);
    }

    @Contract("_, null, _, _ -> false; _, !null, _, _ -> _")
    private boolean playerInDistance(@NotNull Player source, @Nullable Player target, String targetName, boolean dontCheckWhetherIgnored)
    {
        var backend = manager.getCurrentBackend();

        if (target == null
                || (selectionMode.get().equalsIgnoreCase(InteractionMirrorSelectionMode.BY_NAME)
                        ? ignoreDisguised.get() && backend.isDisguised(target)
                        : !match(target, targetName))
                || (!dontCheckWhetherIgnored && (ignoredPlayers.contains(target) || ignoredPlayers.contains(source)))
                || !source.hasPermission(CommonPermissions.MIRROR)
                || target.hasPermission(CommonPermissions.MIRROR_IMMUNE)
                || target.getOpenInventory().getType() != InventoryType.CRAFTING
                || target.isSleeping())
        {
            return false;
        }

        var isInSameWorld = target.getWorld().equals(source.getWorld());
        var normalDistance = config.getOrDefault(Integer.class, ConfigOption.MIRROR_CONTROL_DISTANCE);

        //normalDistance为-1，总是启用，为0则禁用
        return normalDistance == -1
                || (normalDistance != 0 && isInSameWorld && target.getLocation().distance(source.getLocation()) <= normalDistance);
    }

    private final Bindable<Boolean> allowSimulation = new Bindable<>(false);
    private final Bindable<Boolean> allowSneak = new Bindable<>(false);
    private final Bindable<Boolean> allowSwap = new Bindable<>(false);
    private final Bindable<Boolean> allowDrop = new Bindable<>(false);
    private final Bindable<Boolean> allowHotBar = new Bindable<>(false);
    private final Bindable<Boolean> ignoreDisguised = new Bindable<>(false);
    private final Bindable<String> selectionMode = new Bindable<>(InteractionMirrorSelectionMode.BY_NAME);
    private final Bindable<Boolean> logOperations = new Bindable<>(false);

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);

        config.bind(allowSimulation, ConfigOption.MIRROR_BEHAVIOR_DO_SIMULATION);
        config.bind(allowSneak, ConfigOption.MIRROR_BEHAVIOR_SNEAK);
        config.bind(allowSwap, ConfigOption.MIRROR_BEHAVIOR_SWAP_HAND);
        config.bind(allowDrop, ConfigOption.MIRROR_BEHAVIOR_DROP);
        config.bind(allowHotBar, ConfigOption.MIRROR_BEHAVIOR_HOTBAR);
        config.bind(ignoreDisguised, ConfigOption.MIRROR_IGNORE_DISGUISED);

        config.bind(selectionMode, ConfigOption.MIRROR_SELECTION_MODE);

        config.bind(logOperations, ConfigOption.MIRROR_LOG_OPERATION);
    }

    private void update()
    {
        /*

        uuidDisguiseStateMap.forEach((p, d) ->
        {
            var targetPlayer = Bukkit.getPlayerExact(d.getName());

            if (targetPlayer != null)
            {
                targetPlayer.setHealthScale(p.getHealthScale());
                targetPlayer.setFireTicks(p.getFireTicks());
                targetPlayer.setGlowing(p.isGlowing());
                targetPlayer.setGliding(p.isGliding());
                targetPlayer.setSwimming(p.isSwimming());
                targetPlayer.setSprinting(p.isSprinting());
            }
        });

        */

        this.addSchedule(this::update);
        lastRightClick.clear();
        ignoredPlayers.clear();

        if (plugin.getCurrentTick() % (30 * 20) == 0)
            pushToLoggingBase();
    }

    @EventHandler
    public void onPlayerMorph(PlayerMorphEvent e)
    {
        var state = e.getState();
        var id = state.getDisguiseIdentifier();

        if (DisguiseTypes.fromId(id) == DisguiseTypes.PLAYER)
            uuidDisguiseStateMap.put(e.getPlayer(), DisguiseTypes.PLAYER.toStrippedId(id));
        else
            uuidDisguiseStateMap.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerUnMorph(PlayerUnMorphEvent e)
    {
        uuidDisguiseStateMap.remove(e.getPlayer());
    }

    //region Operation Logging

    private final Map<Player, Stack<MirrorSingleEntry>> tempEntries = new Object2ObjectOpenHashMap<>();

    private File plainTextFile;

    private boolean noOperationSinceLastPush;

    private void aaa()
    {
        var dataFolderUri = this.plugin.getDataFolder().toURI();
        this.plainTextFile = new File(URI.create(dataFolderUri + "/mirror_history.log"));

        if (!this.plainTextFile.exists())
        {
            try
            {
                this.plainTextFile.createNewFile();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    public void pushToLoggingBase()
    {
        if (plainTextFile == null)
            aaa();

        synchronized (tempEntries)
        {
            var dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");

            if (tempEntries.isEmpty())
            {
                noOperationSinceLastPush = true;
                return;
            }

            noOperationSinceLastPush = true;

            tempEntries.forEach((p, s) ->
            {
                try (var stream = new FileOutputStream(this.plainTextFile, true))
                {
                    for (var entry : s)
                    {
                        String msg = "";
                        if (noOperationSinceLastPush) msg += "\n";

                        msg += "[%s] %s triggered operation %s for player %s repeating %s time(s).\n"
                                .formatted(dateFormat.format(new Date(entry.timeMills())),
                                        entry.playerName(), entry.operationType(),
                                        entry.targetPlayerName(), entry.repeatingTimes());

                        noOperationSinceLastPush = false;

                        stream.write(msg.getBytes());
                    }
                }
                catch (Throwable throwable)
                {
                    throwable.printStackTrace();
                }
            });

            this.tempEntries.clear();
        }
    }

    @NotNull
    private MirrorSingleEntry getOrCreateEntryFor(Player player, Player targetPlayer, OperationType type)
    {
        synchronized (tempEntries)
        {
            var playerStack = tempEntries.getOrDefault(player, null);

            if (playerStack == null)
            {
                playerStack = new Stack<>();
                tempEntries.put(player, playerStack);
            }

            MirrorSingleEntry entry = null;

            if (playerStack.size() > 0)
            {
                var peek = playerStack.peek();
                if (peek.uuid().equals(player.getUniqueId().toString())
                        && peek.targetPlayerName().equals(targetPlayer.getName())
                        && peek.operationType() == type)
                {
                    entry = peek;
                }
            }

            if (entry != null) return entry;

            entry = new MirrorSingleEntry(player.getName(), player.getUniqueId().toString(), targetPlayer.getName(), type, 0, System.currentTimeMillis());
            playerStack.push(entry);

            return entry;
        }
    }

    private void logOperation(Player source, Player targetPlayer, OperationType type)
    {
        if (!logOperations.get()) return;

        var entry = getOrCreateEntryFor(source, targetPlayer, type);
        entry.increaseRepeatingTimes();
    }

    //endregion Operation Logging
}
