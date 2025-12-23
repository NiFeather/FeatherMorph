package xyz.nifeather.morph.network.multiInstance.slave;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.storage.IPlayerDataBackend;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VoidDataHolder implements IPlayerDataBackend
{
    @Override
    public @Nullable DisguiseMeta getDisguiseMeta(String rawString)
    {
        return null;
    }

    @Override
    public CompletableFuture<PlayerMeta> loadAsync(UUID uuid)
    {
        return CompletableFuture.completedFuture(new PlayerMeta());
    }

    /**
     * Gets the existing data cached in this backend, otherwise call {@link IPlayerDataBackend#loadAsync(UUID)}
     */
    @Override
    public CompletableFuture<PlayerMeta> getOrLoad(UUID uuid)
    {
        return CompletableFuture.completedFuture(new PlayerMeta());
    }

    /**
     * Gets the target UUID's player meta, {@code null} if not loaded
     *
     * @param uuid
     */
    @Override
    public @Nullable PlayerMeta getIfLoaded(UUID uuid)
    {
        return null;
    }

    /**
     * WIP experimental
     *
     * @param player
     * @param disguiseIdentifier
     * @return
     */
    @Override
    public CompletableFuture<Boolean> grantMorphToPlayerAsync(UUID player, String disguiseIdentifier)
    {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> revokeMorphFromPlayerAsync(UUID player, String disguiseIdentifier)
    {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public boolean reload()
    {
        return false;
    }

    @Override
    public boolean save()
    {
        return false;
    }

}
