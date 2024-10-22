package xyz.nifeather.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerTracker
{
    public static PlayerTracker instance()
    {
        return TracketLoader.trackerInstance;
    }

    private static final class TracketLoader
    {
        public static final PlayerTracker trackerInstance = new PlayerTracker();
    }

    public PlayerTracker()
    {
        validPlayers.addAll(Bukkit.getOnlinePlayers());
    }

    private final List<Player> validPlayers = ObjectLists.synchronize(new ObjectArrayList<>());

    public final List<Player> getPlayers()
    {
        return new ObjectArrayList<>(validPlayers);
    }

    public void addPlayer(Player player)
    {
        validPlayers.add(player);
    }

    public void removePlayer(Player player)
    {
        validPlayers.remove(player);
    }
}
