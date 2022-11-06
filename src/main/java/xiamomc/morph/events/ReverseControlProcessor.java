package xiamomc.morph.events;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.PlayerOperationSimulator;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReverseControlProcessor extends MorphPluginObject implements Listener
{
    private final Map<Player, String> uuidDisguiseStateMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        if (!allowSneak) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state);

            if (!playerInDistance(e.getPlayer(), player)) return;

            player.setSneaking(e.isSneaking());
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        if (!allowSwap) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state);

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
        if (!allowDrop) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state);

            if (!playerInDistance(e.getPlayer(), player)) return;

            if (!player.getEquipment().getItemInMainHand().getType().isAir())
                player.dropItem(false);
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent e)
    {
        if (!allowHotBar) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state);

            if (!playerInDistance(e.getPlayer(), player)) return;

            player.getInventory().setHeldItemSlot(e.getNewSlot());
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private PlayerTracker breakingTracker;

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
        if (!swingHands) return;

        if (e.getDamager() instanceof Player damager)
        {
            var state = uuidDisguiseStateMap.get(damager);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayer(state);

                if (!playerInDistance(damager, targetPlayer)) return;

                //如果伪装的玩家想攻击本体，取消事件
                if (e.getEntity() instanceof Player hurtedPlayer && hurtedPlayer.equals(targetPlayer))
                    e.setCancelled(true);

                var damagerLookingAt = damager.getTargetEntity(3);
                var playerLookingAt = targetPlayer.getTargetEntity(3);

                //如果伪装的玩家想攻击的实体和被伪装的玩家一样，取消事件
                if (damagerLookingAt != null && damagerLookingAt.equals(playerLookingAt))
                    e.setCancelled(true);
            }
        }
    }

    @Resolved
    private PlayerTracker tracker;

    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        if (!swingHands) return;

        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(player);

        if (state != null)
        {
            var targetPlayer = Bukkit.getPlayer(state);

            var lastAction = playerActionMap.get(player);

            if (lastAction == null) return;

            if (!tracker.isPlayerInteractingAnything(player))
                simulateOperation(lastAction, targetPlayer, EquipmentSlot.HAND);
        }
    }

    /**
     * 玩家 -> 上次交互时的动作（左/右键）
     */
    private final Map<Player, Action> playerActionMap = new Object2ObjectOpenHashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.HAND)
        {
            var player = e.getPlayer();
            var state = uuidDisguiseStateMap.get(player);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayer(state);

                if (!playerInDistance(player, targetPlayer)) return;

                //todo: 实现副手动作
                var targetHand = EquipmentSlot.HAND;
                playerActionMap.put(player, e.getAction());

                simulateOperation(e.getAction(), targetPlayer, targetHand);
            }
        }
    }

    @Resolved
    private PlayerOperationSimulator operationSimulator;

    private void simulateOperation(Action action, Player targetPlayer, EquipmentSlot hand)
    {
        if (action.isRightClick())
        {
            if (operationSimulator.simulateRightClick(targetPlayer, hand))
                targetPlayer.swingMainHand();
        }
        else
        {
            if (operationSimulator.simulateLeftClick(targetPlayer, hand))
                targetPlayer.swingMainHand();
        }
    }

    private boolean playerInDistance(@NotNull Player source, @Nullable Player target)
    {
        if (target == null || (DisguiseAPI.isDisguised(target) && ignoreDisguised)) return false;

        var isInSameWorld = target.getWorld().equals(source.getWorld());
        var targetHelmet = target.getEquipment().getHelmet();

        //-1: 总是启用，0: 禁用
        if (targetHelmet != null && immuneItemMaterial != null && targetHelmet.getType().equals(immuneItemMaterial))
        {
            var immuneDistance = config.getOrDefault(Integer.class, ConfigOption.REVERSE_CONTROL_DISTANCE_IMMUNE);

            //immuneDistance为-1，总是启用，为0则禁用
            return immuneDistance == -1
                    || (immuneDistance != 0 && isInSameWorld && target.getLocation().distance(source.getLocation()) <= immuneDistance);
        }
        else
        {
            var normalDistance = config.getOrDefault(Integer.class, ConfigOption.REVERSE_CONTROL_DISTANCE);

            //normalDistance为-1，总是启用，为0则禁用
            return normalDistance == -1
                    || (normalDistance != 0 && isInSameWorld && target.getLocation().distance(source.getLocation()) <= normalDistance);
        }
    }

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        config.onConfigRefresh(c -> onConfigUpdate(), true);
    }

    private Material immuneItemMaterial;

    private boolean allowSimulation;
    private boolean swingHands;
    private boolean allowSneak;
    private boolean allowSwap;
    private boolean allowDrop;
    private boolean allowHotBar;
    private boolean ignoreDisguised;

    private void onConfigUpdate()
    {
        var immune = config.getOrDefault(String.class, ConfigOption.REVERSE_CONTROL_IMMUNE_ITEM);
        var targetOptional = Material.matchMaterial(immune);

        if (targetOptional == null)
            logger.warn("未能找到和" + immune + "对应的免疫物品，相关功能将不会启用");

        immuneItemMaterial = targetOptional;

        this.allowSimulation = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_DO_SIMULATION);
        this.swingHands = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_SWING_HANDS);
        this.allowSneak = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_SNEAK);
        this.allowSwap = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_SWAP_HAND);
        this.allowDrop = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_DROP);
        this.allowHotBar = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_BEHAVIOR_HOTBAR);
        this.ignoreDisguised = config.getOrDefault(Boolean.class, ConfigOption.REVERSE_IGNORE_DISGUISED);
    }

    private void update()
    {
        /*

        uuidDisguiseStateMap.forEach((p, d) ->
        {
            var targetPlayer = Bukkit.getPlayer(d.getName());

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

        this.addSchedule(c -> update());
    }

    @EventHandler
    public void onPlayerMorph(PlayerMorphEvent e)
    {
        if (e.getState().getDisguise().isPlayerDisguise())
        {
            uuidDisguiseStateMap.put(e.getPlayer(), ((PlayerDisguise)e.getState().getDisguise()).getName());
        }
    }

    @EventHandler
    public void onPlayerUnMorph(PlayerUnMorphEvent e)
    {
        uuidDisguiseStateMap.remove(e.getPlayer());
    }
}
