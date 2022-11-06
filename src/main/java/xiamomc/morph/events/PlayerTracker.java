package xiamomc.morph.events;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家互动跟踪器
 */
public class PlayerTracker extends MorphPluginObject implements Listener
{
    /**
     * 某人是否疑似正在破坏方块？
     */
    private final Map<Player, Long> breakingSuspectList = new ConcurrentHashMap<>();

    /**
     * 玩家上次左/右键的时间
     */
    private final Map<Player, Long> lastInteractTime = new ConcurrentHashMap<>();

    /**
     * 玩家上次互动的动作类型
     */
    private final Map<Player, Action> lastInteractAction = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        var player = e.getPlayer();

        lastInteractTime.put(player, plugin.getCurrentTick());
        lastInteractAction.put(player, e.getAction());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e)
    {
        if (e.getDamager() instanceof Player player)
        {
            lastInteractTime.put(player, plugin.getCurrentTick());
            lastInteractAction.put(player, Action.LEFT_CLICK_AIR);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        var player = e.getPlayer();

        lastInteractTime.put(player, plugin.getCurrentTick());
        lastInteractAction.put(player, Action.RIGHT_CLICK_AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e)
    {
        var player = e.getPlayer();

        lastInteractTime.put(player, plugin.getCurrentTick());
        lastInteractAction.put(player, Action.RIGHT_CLICK_AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e)
    {
        breakingSuspectList.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        var player = e.getPlayer();

        if (!e.getHand().equals(EquipmentSlot.HAND)) return;

        //更新玩家破坏时间
        if (breakingSuspectList.containsKey(player))
            breakingSuspectList.put(player, plugin.getCurrentTick());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDrop(PlayerDropItemEvent e)
    {
        var player = e.getPlayer();

        lastInteractTime.remove(player);
        lastInteractAction.remove(player);
    }

    private final Map<Player, Long> spectatingPlayers = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerStartSpectate(PlayerStartSpectatingEntityEvent e)
    {
        var player = e.getPlayer();

        spectatingPlayers.put(player, plugin.getCurrentTick());
        lastInteractTime.put(player, plugin.getCurrentTick());
        lastInteractAction.put(player, Action.LEFT_CLICK_AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerStopSpectate(PlayerStopSpectatingEntityEvent e)
    {
        spectatingPlayers.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();

        if (player.getSpectatorTarget() == null)
            spectatingPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerExit(PlayerQuitEvent e)
    {
        var player = e.getPlayer();

        lastInteractAction.remove(player);
        lastInteractTime.remove(player);
        breakingSuspectList.remove(player);
    }

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());
    }

    /**
     * 玩家是否在这一刻开始旁观？
     * @param player 目标玩家
     * @return 是否在这一刻开始旁观
     */
    public boolean playerStartingSpectating(Player player)
    {
        var currentTick = plugin.getCurrentTick();

        return player.getSpectatorTarget() != null
                && currentTick - spectatingPlayers.getOrDefault(player, currentTick) <= 0;
    }

    /**
     * 玩家这一刻是否在和任何东西互动?
     *
     * @param player 要查询的玩家
     * @return 是否在和方块互动
     */
    public boolean isPlayerInteractingAnything(Player player)
    {
        return plugin.getCurrentTick() - lastInteractTime.getOrDefault(player, plugin.getCurrentTick()) <= 0;
    }

    @Nullable
    public Action getLastInteractAction(Player player)
    {
        return lastInteractAction.get(player);
    }

    private void update()
    {
        var playerToRemoveFromSuspect = new ObjectArrayList<Player>();

        var currentTick = plugin.getCurrentTick();

        breakingSuspectList.forEach((p, l) ->
        {
            if (currentTick - l > 2) playerToRemoveFromSuspect.add(p);
        });

        playerToRemoveFromSuspect.forEach(breakingSuspectList::remove);

        this.addSchedule(c -> update());
    }
}
