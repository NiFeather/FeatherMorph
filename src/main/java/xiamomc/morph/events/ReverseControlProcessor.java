package xiamomc.morph.events;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.PlayerOperationSimulator;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetSneakingCommand;
import xiamomc.morph.utilities.ItemUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReverseControlProcessor extends MorphPluginObject implements Listener
{
    private final Map<Player, String> uuidDisguiseStateMap = new ConcurrentHashMap<>();

    @Resolved
    private MorphClientHandler clientHandler;

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        if (!allowSneak.get()) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayerExact(state);

            if (!playerInDistance(e.getPlayer(), player) || player.isSneaking() == e.isSneaking()) return;

            player.setSneaking(e.isSneaking());
            clientHandler.sendClientCommand(player, new S2CSetSneakingCommand(e.isSneaking()));
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        if (!allowSwap.get()) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayerExact(state);

            if (!playerInDistance(e.getPlayer(), player)) return;

            var equipment = player.getEquipment();

            var mainHandItem = equipment.getItemInMainHand();
            var offhandItem = equipment.getItemInOffHand();

            equipment.setItemInMainHand(offhandItem);
            equipment.setItemInOffHand(mainHandItem);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e)
    {
        if (!allowDrop.get()) return;

        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var target = Bukkit.getPlayerExact(state);

            if (!playerInDistance(player, target) || !player.isSneaking()) return;

            if (!target.getEquipment().getItemInMainHand().getType().isAir())
            {
                target.dropItem(false);
                target.swingHand(EquipmentSlot.HAND);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent e)
    {
        if (!allowHotBar.get()) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayerExact(state);

            if (!playerInDistance(e.getPlayer(), player)) return;

            player.getInventory().setHeldItemSlot(e.getNewSlot());
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
    public void onPlayerHurtPlayer(EntityDamageByEntityEvent e)
    {
        if (!allowSimulation.get()) return;

        if (e.getDamager() instanceof Player damager)
        {
            var state = uuidDisguiseStateMap.get(damager);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayerExact(state);

                if (!playerInDistance(damager, targetPlayer)) return;

                //如果伪装的玩家想攻击本体，取消事件并模拟左键
                if (e.getEntity() instanceof Player hurtedPlayer && hurtedPlayer.equals(targetPlayer))
                {
                    simulateOperation(Action.LEFT_CLICK_AIR, targetPlayer);

                    e.setCancelled(true);

                    return;
                }

                var damagerLookingAt = damager.getTargetEntity(3);
                var playerLookingAt = targetPlayer.getTargetEntity(3);

                //如果伪装的玩家想攻击的实体和被伪装的玩家一样，模拟左键并取消事件
                if (damagerLookingAt != null && damagerLookingAt.equals(playerLookingAt))
                {
                    simulateOperation(Action.LEFT_CLICK_AIR, targetPlayer);

                    e.setCancelled(true);
                }
            }
        }
    }

    @Resolved
    private PlayerTracker tracker;

    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        if (!allowSimulation.get()) return;

        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(player);

        if (state != null)
        {
            var targetPlayer = Bukkit.getPlayerExact(state);

            if (!playerInDistance(player, targetPlayer)) return;

            var lastAction = tracker.getLastInteractAction(player);

            if (lastAction == null) return;

            //旁观者模式下左键方块不会产生Interact事件，我们得猜这个玩家现在是左键还是右键
            if (player.getGameMode() == GameMode.SPECTATOR)
            {
                if (lastAction.isRightClick())
                    lastAction = PlayerTracker.InteractType.LEFT_CLICK_BLOCK;
            }

            //避免重复镜像
            if (!tracker.interactingThisTick(player))
                simulateOperation(lastAction.toBukkitAction(), targetPlayer);

            //如果玩家在被控玩家一定范围以内，被控玩家有目标实体，并且玩家没有目标实体，那么取消挥手动画
            if (targetPlayer.getLocation().getWorld() == player.getLocation().getWorld()
                && Math.abs(targetPlayer.getLocation().distanceSquared(player.getLocation())) <= 6)
            {
                var theirTarget = targetPlayer.getTargetEntity(3);
                var ourTarget = player.getTargetEntity(3);

                if (theirTarget != null
                        && (ourTarget == null || ourTarget == targetPlayer || ourTarget == theirTarget))
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.HAND)
        {
            var player = e.getPlayer();
            var state = uuidDisguiseStateMap.get(player);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayerExact(state);

                if (!playerInDistance(player, targetPlayer)) return;

                simulateOperation(e.getAction(), targetPlayer);
            }
        }
    }

    @Resolved
    private PlayerOperationSimulator operationSimulator;

    /**
     * 模拟玩家操作
     *
     * @param action 操作类型
     * @param targetPlayer 目标玩家
     * @return 操作是否成功
     */
    private boolean simulateOperation(Action action, Player targetPlayer)
    {
        if (!allowSimulation.get()) return false;

        var isRightClick = action.isRightClick();
        var result = isRightClick
                ? operationSimulator.simulateRightClick(targetPlayer)
                : operationSimulator.simulateLeftClick(targetPlayer);

        if (result.success())
        {
            var itemInUse = targetPlayer.getEquipment().getItem(result.hand()).getType();

            if (!isRightClick || !ItemUtils.noSwingType(itemInUse))
                targetPlayer.swingHand(result.hand());

            return true;
        }

        return false;
    }

    private boolean playerInDistance(@NotNull Player source, @Nullable Player target)
    {
        if (target == null
                || (DisguiseAPI.isDisguised(target) && ignoreDisguised.get())
                || !source.hasPermission(CommonPermissions.REVERSE)
                || target.hasPermission(CommonPermissions.REVERSE_IMMUNE)
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
}
