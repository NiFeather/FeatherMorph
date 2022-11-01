package xiamomc.morph.events;

import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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

    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        if (!swingHands) return;

        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(player);

        if (state != null)
        {
            var targetPlayer = Bukkit.getPlayer(state);
            var shouldAttack = false;

            if(!playerInDistance(player, targetPlayer)) return;

            if (targetPlayer.getGameMode() == GameMode.SURVIVAL
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
                var targetPlayer = Bukkit.getPlayer(state);

                if (!playerInDistance(player, targetPlayer)) return;

                var targetHand = clientHandRelativeToTarget(e.getHand(), player, targetPlayer);
                if (simulateRightClick(targetPlayer, targetHand))
                    targetPlayer.swingHand(targetHand);
            }
        }
    }

    //模拟玩家右键
    private boolean simulateRightClick(Player player, EquipmentSlot targetHand)
    {
        if (!allowSimulation) return false;

        if (targetHand != EquipmentSlot.HAND && targetHand != EquipmentSlot.OFF_HAND)
            throw new IllegalArgumentException("给定的targetHand不是主手或副手");

        //获取正在看的方块或实体
        var eyeLoc = player.getEyeLocation();
        var traceResult = player.getWorld().rayTrace(eyeLoc, eyeLoc.getDirection(), 3,
                FluidCollisionMode.NEVER, false, 0d, Predicate.not(Predicate.isEqual(player)));

        var targetBlock = traceResult == null ? null : traceResult.getHitBlock();
        var targetEntity = traceResult == null ? null : traceResult.getHitEntity();

        //Player in fabric mojang mappings
        var playerHumanHandle = ((CraftHumanEntity) player).getHandle();

        //ServerPlayer in mojang mappings
        var playerHandle = ((CraftPlayer) player).getHandle();

        //ServerLevel in mojang mappings
        var worldHandle = ((CraftWorld)player.getWorld()).getHandle();

        //GameMode in fabric mojang mappings
        var mgr = playerHandle.d;

        var hand = targetHand == EquipmentSlot.HAND ? EnumHand.a : EnumHand.b;
        var item = player.getEquipment().getItem(targetHand);

        var pluginManager = Bukkit.getPluginManager();

        //如果目标实体不是null，则和实体互动
        if (targetEntity != null)
        {
            var entityHandle = ((CraftEntity)targetEntity).getHandle();

            //获取目标位置
            var hitPos = traceResult.getHitPosition();

            //获取互动位置
            hitPos.subtract(targetEntity.getLocation().toVector());

            var vec = new Vec3D(hitPos.getX(), hitPos.getY(), hitPos.getZ());

            //先试试InteractAt
            //如果不成功，调用Interact
            //最后试试useItem
            var interactEntityEvent = new PlayerInteractEntityEvent(player, targetEntity);
            var interactAtEvent = new PlayerInteractAtEntityEvent(player, targetEntity, hitPos);
            pluginManager.callEvent(interactEntityEvent);
            pluginManager.callEvent(interactAtEvent);

            if (!interactAtEvent.isCancelled() && !interactEntityEvent.isCancelled())
            {
                return entityHandle.a(playerHandle, vec, EnumHand.a).a()
                        || playerHumanHandle.a(entityHandle, EnumHand.a).a()
                        || mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand).a();
            }
            else
                return false;
        }

        boolean success = false;

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

                var event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, targetBlock, bukkitFace);
                pluginManager.callEvent(event);

                //ServerPlayerGameMode.useItemOn()
                if (!event.isCancelled())
                    success = mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand, moving).a();
            }
        }

        //ServerPlayerGameMode.useItem()
        if (!success)
        {
            var event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF);
            pluginManager.callEvent(event);

            if (!event.isCancelled())
                return mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand).a();
            else
                return false;
        }
        else
            return true;
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
