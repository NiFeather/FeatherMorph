package xyz.nifeather.morph.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerFutures
{
    private static final Map<UUID, CompletableFuture<Player>> waitMap = new ConcurrentHashMap<>();

    public static CompletableFuture<Player> waitPlayer(UUID player)
    {
        var existingPlayer = Bukkit.getPlayer(player);
        if (existingPlayer != null)
            return CompletableFuture.completedFuture(existingPlayer);

        var future = new CompletableFuture<Player>();
        waitMap.put(player, future);

        return future;
    }

    public static void complete(Player player)
    {
        var future = waitMap.getOrDefault(player.getUniqueId(), null);
        if (future != null)
            future.complete(player);
    }
}
