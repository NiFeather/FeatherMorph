package xyz.nifeather.morph.misc;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.networking.exceptions.OwnerDiscardedException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerWaitingHandler<TPlayer>
{
    private final Map<TPlayer, CompletableFuture<TPlayer>> waitMap = new ConcurrentHashMap<>();

    private CompletableFuture<TPlayer> getOrCreateWaitingFuture(TPlayer player)
    {
        var existing = waitMap.getOrDefault(player, null);
        if (existing != null) return existing;

        var newFuture = new CompletableFuture<TPlayer>();
        waitMap.put(player, newFuture);
        return newFuture;
    }

    public CompletableFuture<TPlayer> getWaitingFuture(TPlayer player)
    {
        return getOrCreateWaitingFuture(player);
    }

    public void discard(TPlayer player, @Nullable Exception reason)
    {
        var existing = waitMap.getOrDefault(player, null);
        if (existing != null && !existing.isDone()) // We don't want to trigger exception for completed futures.
            existing.completeExceptionally(reason != null ? reason : new OwnerDiscardedException("The parent of this waiting future has been discarded for unknown reason"));

        this.waitMap.remove(player);
    }

    public void completeFuture(TPlayer player)
    {
        getOrCreateWaitingFuture(player).complete(player);
    }
}
