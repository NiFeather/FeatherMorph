package xiamomc.morph.events;

import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.EnumHand;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReverseControlProcessor extends MorphPluginObject implements Listener
{
    private final Map<Player, PlayerDisguise> uuidDisguiseStateMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        if (!allowSneak) return;

        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state.getName());

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
            var player = Bukkit.getPlayer(state.getName());

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
            var player = Bukkit.getPlayer(state.getName());

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
            var player = Bukkit.getPlayer(state.getName());

            if (!playerInDistance(e.getPlayer(), player)) return;

            player.getInventory().setHeldItemSlot(e.getNewSlot());
        }
    }

    @Resolved
    private MorphManager manager;

    @Resolved
    private PlayerTracker breakingTracker;

    @Resolved
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
                var targetPlayer = Bukkit.getPlayer(state.getName());

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

    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        if (!swingHands) return;

        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(player);

        if (state != null)
        {
            var targetPlayer = Bukkit.getPlayer(state.getName());
            var shouldAttack = false;

            if(!playerInDistance(player, targetPlayer)) return;

            if (manager.getDisguiseStateFor(targetPlayer) == null
                    && targetPlayer.getGameMode() == GameMode.SURVIVAL
                    && playerInDistance(player, targetPlayer))
            {
                var swingMainHand = player.getClientOption(ClientOption.MAIN_HAND)
                        .equals(targetPlayer.getClientOption(ClientOption.MAIN_HAND));

                var targetEntity = targetPlayer.getTargetEntity(3);

                if (!breakingTracker.playerStartingSpectating(player))
                    targetPlayer.swingHand(swingMainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);

                var cancelSwing = targetEntity != null && !breakingTracker.isPlayerInteracting(player);

                shouldAttack = cancelSwing
                        && (swingMainHand || targetPlayer.getEquipment().getItemInMainHand().isSimilar(targetPlayer.getEquipment().getItemInOffHand()));

                if (shouldAttack)
                    targetPlayer.attack(targetEntity);

                e.setCancelled(e.isCancelled() || cancelSwing);
            }
        }
    }

    private EquipmentSlot clientHandRelativeToTarget(EquipmentSlot slot, Player source, Player target)
    {
        if (slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND)
            throw new IllegalArgumentException();

        var swingMainHand = source.getClientOption(ClientOption.MAIN_HAND)
                .equals(target.getClientOption(ClientOption.MAIN_HAND));

        return slot == EquipmentSlot.HAND
                ? swingMainHand ? slot : EquipmentSlot.OFF_HAND
                : swingMainHand ? EquipmentSlot.OFF_HAND : slot;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.HAND && e.getAction().isRightClick())
        {
            var player = e.getPlayer();
            var state = uuidDisguiseStateMap.get(player);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayer(state.getName());

                if (!playerInDistance(player, targetPlayer)) return;

                var targetHand = clientHandRelativeToTarget(e.getHand(), player, targetPlayer);
                if (simulateInteract(targetPlayer, targetHand))
                    targetPlayer.swingHand(targetHand);
            }
        }
    }

    //模拟玩家右键
    private boolean simulateInteract(Player player, EquipmentSlot targetHand)
    {
        if (!allowSimulation) return false;

        if (targetHand != EquipmentSlot.HAND && targetHand != EquipmentSlot.OFF_HAND)
            throw new IllegalArgumentException("给定的targetHand不是主手或副手");

        var targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
        var targetEntity = player.getTargetEntity(3);

        //EntityHuman in spigot == Player in fabric mojang mappings
        var playerHumanHandle = ((CraftHumanEntity) player).getHandle();
        var playerHandle = ((CraftPlayer) player).getHandle();
        var worldHandle = ((CraftWorld)player.getWorld()).getHandle();

        if (targetEntity != null)
        {
            var entityHandle = ((CraftEntity)targetEntity).getHandle();

            //实体之间的距离
            var distance = targetEntity.getLocation().distance(player.getLocation());

            //tan $angle = (x / $distance) = $tan -> x = ($distance * $tan) -> $val
            //$height - $val -> $targetHeight
            var angle = player.getEyeLocation().getPitch();

            if (angle == 90f) angle = 89.999f;
            else if (angle == -90f) angle = -89.999f;

            var tan = Math.tan(Math.toRadians(angle));
            var val = targetEntity.getHeight() - distance * tan;

            //todo: 计算视线与目标接触的的X、Z值
            var vec = new Vec3D(0, val, 0);

            //先试试InteractAt
            //如果不成功，调用Interact
            if (!entityHandle.a(playerHandle, vec, EnumHand.a).a())
                return playerHumanHandle.a(entityHandle, EnumHand.a).a();
            else
                return true;
        }

        boolean success = false;

        var hand = targetHand == EquipmentSlot.HAND ? EnumHand.a : EnumHand.b;
        var item = player.getEquipment().getItem(targetHand);

        //GameMode in fabric mojang mappings
        var mgr = playerHandle.d;

        if (targetBlock != null)
        {
            var loc = ((CraftBlock) targetBlock).getPosition();

            EnumDirection targetBlockDirection = null;
            var bukkitFace = player.getTargetBlockFace(5);
            bukkitFace = bukkitFace == null ? BlockFace.SELF : bukkitFace;

            //BlockFace(Bukkit)转BlockFace(Minecraft)
            switch (bukkitFace)
            {
                case DOWN -> targetBlockDirection = EnumDirection.a;
                case UP -> targetBlockDirection = EnumDirection.b;
                case NORTH -> targetBlockDirection = EnumDirection.c;
                case SOUTH -> targetBlockDirection = EnumDirection.d;
                case WEST -> targetBlockDirection = EnumDirection.e;
                case EAST -> targetBlockDirection = EnumDirection.f;
                default -> logger.error("未知的BlockFace: " + bukkitFace + ", 将不会尝试使用useItemOn");
            }

            if (targetBlockDirection != null)
            {
                var moving = new MovingObjectPositionBlock(
                        new Vec3D(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()),
                        targetBlockDirection, loc, false);

                //ServerPlayerGameMode.useItemOn()
                success = mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand, moving).a();
            }
        }

        //ServerPlayerGameMode.useItem()
        if (!success)
            return mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand).a();
        else
            return true;
    }

    private boolean playerInDistance(@NotNull Player source, @Nullable Player target)
    {
        if (target == null) return false;

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
            uuidDisguiseStateMap.put(e.getPlayer(), (PlayerDisguise) e.getState().getDisguise());
        }
    }

    @EventHandler
    public void onPlayerUnMorph(PlayerUnMorphEvent e)
    {
        uuidDisguiseStateMap.remove(e.getPlayer());
    }
}
