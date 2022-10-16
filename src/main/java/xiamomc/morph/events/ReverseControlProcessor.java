package xiamomc.morph.events;

import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
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

    /*

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state.getName());

            if (player != null) player.setSneaking(e.isSneaking());
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state.getName());

            if (player != null)
            {
                var equipment = player.getEquipment();

                var mainHandItem = equipment.getItemInMainHand();
                var offhandItem = equipment.getItemInOffHand();

                equipment.setItemInMainHand(offhandItem);
                equipment.setItemInOffHand(mainHandItem);
            }
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e)
    {
        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state.getName());

            if (player != null)
                player.dropItem(false);
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent e)
    {
        var state = uuidDisguiseStateMap.get(e.getPlayer());

        if (state != null)
        {
            var player = Bukkit.getPlayer(state.getName());

            if (player != null)
            {
                player.getInventory().setHeldItemSlot(e.getNewSlot());
            }
        }
    }

    */

    @Resolved
    private MorphManager manager;

    @Resolved
    private PlayerTracker breakingTracker;

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        uuidDisguiseStateMap.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerHurtPlayer(EntityDamageByEntityEvent e)
    {
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player hurtedPlayer)
        {
            var state = uuidDisguiseStateMap.get(damager);

            if (state != null)
            {
                var targetPlayer = Bukkit.getPlayer(state.getName());

                if (targetPlayer == null) return;

                //如果伪装的玩家想攻击本体，取消事件
                if (hurtedPlayer.equals(targetPlayer))
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
        var player = e.getPlayer();
        var state = uuidDisguiseStateMap.get(player);

        if (state != null)
        {
            var targetPlayer = Bukkit.getPlayer(state.getName());
            var shouldAttack = false;

            if (targetPlayer != null
                    && manager.getDisguiseStateFor(targetPlayer) == null
                    && targetPlayer.getGameMode() == GameMode.SURVIVAL
                    && playerInDistance(player, targetPlayer))
            {
                var clientMainHand = player.getClientOption(ClientOption.MAIN_HAND);
                var targetMainHand = targetPlayer.getClientOption(ClientOption.MAIN_HAND);

                var targetEntity = targetPlayer.getTargetEntity(3);

                if (clientMainHand.equals(targetMainHand))
                {
                    if (!breakingTracker.playerStartingSpectating(player))
                        targetPlayer.swingMainHand();

                    shouldAttack = targetEntity != null;
                }
                else
                {
                    targetPlayer.swingOffHand();

                    if (targetPlayer.getEquipment().getItemInMainHand().getType().isAir()
                        && targetPlayer.getEquipment().getItemInOffHand().getType().isAir())
                    {
                        shouldAttack = targetEntity != null;
                    }
                }

                //检查玩家有没有正在破坏方块或者正在和方块互动
                shouldAttack = shouldAttack && !breakingTracker.isPlayerInteracting(player);

                if (shouldAttack)
                    targetPlayer.attack(targetEntity);

                e.setCancelled(e.isCancelled() || shouldAttack);
            }
        }
    }

    @Resolved
    private MorphConfigManager config;

    private boolean playerInDistance(Player source, Player target)
    {
        var isInSameWorld = target.getWorld().equals(source.getWorld());
        var targetHelmet = target.getEquipment().getHelmet();

        //-1: 总是启用，0: 禁用
        if (targetHelmet != null && targetHelmet.getType().equals(Material.GOLDEN_HELMET))
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
