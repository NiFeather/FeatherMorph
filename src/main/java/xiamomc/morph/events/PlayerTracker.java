package xiamomc.morph.events;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTracker extends MorphPluginObject implements Listener
{
    /**
     * 某人是否疑似正在破坏方块？
     */
    private final Map<Player, Long> breakingSuspectList = new ConcurrentHashMap<>();

    /**
     * 玩家上次互动时间
     */
    private final Map<Player, Long> lastInteract = new ConcurrentHashMap<>();

    @Resolved
    private MorphPlugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.hasBlock())
        {
            lastInteract.put(e.getPlayer(), plugin.getCurrentTick());
        }
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent e)
    {
        lastInteract.put(e.getPlayer(), plugin.getCurrentTick());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        breakingSuspectList.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent e)
    {
        var player = e.getPlayer();

        if (!e.getHand().equals(EquipmentSlot.HAND)) return;

        //更新玩家破坏时间
        if (lastInteract.containsKey(player) || breakingSuspectList.containsKey(player))
        {
            lastInteract.put(player, plugin.getCurrentTick());
            breakingSuspectList.put(player, plugin.getCurrentTick());
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e)
    {
        lastInteract.put(e.getPlayer(), plugin.getCurrentTick());
    }

    private final Map<Player, Long> spectatingPlayers = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerStartSpectate(PlayerStartSpectatingEntityEvent e)
    {
        spectatingPlayers.put(e.getPlayer(), plugin.getCurrentTick());
        lastInteract.put(e.getPlayer(), plugin.getCurrentTick());
    }

    @EventHandler
    public void onPlayerStopSpectate(PlayerStopSpectatingEntityEvent e)
    {
        spectatingPlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();

        if (player.getSpectatorTarget() == null)
            spectatingPlayers.remove(player);
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        lastInteract.remove(e.getPlayer());
        breakingSuspectList.remove(e.getPlayer());
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
     * 玩家是否在这一刻和方块互动?
     * @param player 要查询的玩家
     * @return 是否在和方块互动
     */
    public boolean isPlayerInteracting(Player player)
    {
        //是否上次互动在这一刻，并且可能在和方块互动？

        return plugin.getCurrentTick() - lastInteract.getOrDefault(player, plugin.getCurrentTick()) <= 0
                && breakingSuspectList.containsKey(player);
    }

    private void update()
    {
        var playerToRemoveFromSuspect = new ObjectArrayList<Player>();
        var playerToRemoveFromInteract = new ObjectArrayList<Player>();

        var currentTick = plugin.getCurrentTick();

        breakingSuspectList.forEach((p, l) ->
        {
            if (currentTick - l > 2) playerToRemoveFromSuspect.add(p);
        });

        lastInteract.forEach((p, l) ->
        {
            if (currentTick - l > 2) playerToRemoveFromInteract.add(p);
        });

        playerToRemoveFromSuspect.forEach(breakingSuspectList::remove);
        playerToRemoveFromInteract.forEach(lastInteract::remove);

        this.addSchedule(c -> update());
    }
}
