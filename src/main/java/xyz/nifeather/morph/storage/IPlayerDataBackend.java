package xyz.nifeather.morph.storage;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlayerDataBackend
{
    /**
     * 获取伪装信息
     *
     * @param rawString 原始ID
     * @return 伪装信息
     * @apiNote 如果原始ID不是有效ID，则会返回null
     */
    @Nullable
    public DisguiseMeta getDisguiseMeta(String rawString);

    /**
     * Load the requested data async
     * @param uuid The target player's UUID
     * @return The matching {@link PlayerMeta}
     * @apiNote The future throws {@link xiamomc.pluginbase.Exceptions.NullDependencyException} If data for the requested UUID cannot be found.
     */
    CompletableFuture<PlayerMeta> loadAsync(UUID uuid);

    /**
     * Get or load data for the given UUID
     */
    CompletableFuture<PlayerMeta> getOrLoad(UUID uuid);

    /**
     * Gets the target UUID's player meta, {@code null} if not loaded
     */
    @Nullable
    PlayerMeta getIfLoaded(UUID uuid);

    /**
     * Ask the remote service to grant the given disguise to the given player
     * @return Whether this operation has finished successfully
     */
    public CompletableFuture<Boolean> grantMorphToPlayerAsync(UUID player, String disguiseIdentifier);

    /**
     * Ask the remote service to revoke the given disguise to the given player
     * @return Whether this operation has finished successfully
     */
    public CompletableFuture<Boolean> revokeMorphFromPlayerAsync(UUID player, String disguiseIdentifier);

    public boolean reload();

    public boolean save();
}
