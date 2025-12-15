package xyz.nifeather.morph.network.multiInstance.slave;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.storage.IPlayerDataBackend;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.network.multiInstance.protocol.Operation;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SSyncDisguiseCommand;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkDataHolder extends MorphPluginObject implements IPlayerDataBackend
{
    private final Map<UUID, PlayerMeta> localMetaMap = new ConcurrentHashMap<>();

    private final SlaveInstance bindingSlave;

    public NetworkDataHolder(SlaveInstance bindingSlave)
    {
        this.bindingSlave = bindingSlave;
    }

    private final Map<String, DisguiseMeta> cachedMetas = new ConcurrentHashMap<>();

    @Override
    public @Nullable DisguiseMeta getDisguiseMeta(String rawString)
    {
        var cached = cachedMetas.getOrDefault(rawString, null);
        if (cached != null) return cached;

        var type = DisguiseTypes.fromId(rawString);

        var meta = new DisguiseMeta(rawString, type);
        cachedMetas.put(rawString, meta);

        return meta;
    }

    @Override
    public CompletableFuture<PlayerMeta> loadAsync(UUID uuid)
    {
        var future = new CompletableFuture<PlayerMeta>();

        bindingSlave.requestData(uuid).thenAccept(u ->
        {
            var data = nullablePlayerMeta(u);

            if (data != null)
                future.complete(data);
            else
                future.completeExceptionally(new NullDependencyException("The future of the requested data has been finished, but we can't find a matching data in NetworkDataHolder!"));
        });

        return future;
    }

    /**
     * Get or load data for the given UUID
     *
     * @param uuid
     */
    @Override
    public CompletableFuture<PlayerMeta> getOrLoad(UUID uuid)
    {
        var existing = nullablePlayerMeta(uuid);
        if (existing != null)
            return CompletableFuture.completedFuture(existing);

        var future = new CompletableFuture<PlayerMeta>();

        bindingSlave.requestData(uuid);
        bindingSlave.getOrCreatePlayerFuture(uuid).thenAccept(u ->
        {
            var data = this.nullablePlayerMeta(u);
            if (data != null)
                future.complete(data);
            else
                future.completeExceptionally(new RuntimeException("Request for %s has been finished, but we can't find it in an instance of NetworkDataHolder".formatted(uuid)));
        });

        return future;
    }

    /**
     * Gets the target UUID's player meta, {@code null} if not loaded
     *
     * @param uuid
     */
    @Override
    public @Nullable PlayerMeta getIfLoaded(UUID uuid)
    {
        return this.nullablePlayerMeta(uuid);
    }

    /**
     * WIP experimental
     *
     * @param disguiseIdentifier
     * @return
     */
    @Override
    public CompletableFuture<Boolean> grantMorphToPlayerAsync(UUID uuid, String disguiseIdentifier)
    {
        if (!bindingSlave.isOnline())
        {
            logger.error("We are not connected with master server! Refusing to update unlock state...");
            return CompletableFuture.completedFuture(false);
        }

        // Don't continue if we don't have got the required metadata
        var meta = nullablePlayerMeta(uuid);
        if (meta == null)
            return CompletableFuture.completedFuture(false);

        if (meta.getUnlockedDisguiseIdentifiers().stream().anyMatch(str -> str.equals(disguiseIdentifier)))
            return CompletableFuture.completedFuture(false);

        bindingSlave.sendCommand(new MIC2SSyncDisguiseCommand(Operation.ADD_IF_ABSENT, List.of(disguiseIdentifier), uuid));

        var future = new CompletableFuture<Boolean>();
        bindingSlave.getOrCreatePlayerFuture(uuid).thenAccept(ignored ->
        {
            var data = this.getOrCreatePlayerMeta(uuid);

            if (data.getUnlockedDisguiseIdentifiers().contains(disguiseIdentifier))
                future.complete(true);
            else
                future.complete(false);
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> revokeMorphFromPlayerAsync(UUID uuid, String disguiseIdentifier)
    {
        if (!bindingSlave.isOnline())
        {
            logger.error("We are not connected with master server! Refusing to update unlock state...");
            return CompletableFuture.completedFuture(false);
        }

        // Don't continue if we don't have got the required metadata
        var meta = nullablePlayerMeta(uuid);
        if (meta == null)
            return CompletableFuture.completedFuture(false);

        if (meta.getUnlockedDisguiseIdentifiers().stream().noneMatch(str -> str.equals(disguiseIdentifier)))
            return CompletableFuture.completedFuture(false);

        bindingSlave.sendCommand(new MIC2SSyncDisguiseCommand(Operation.REMOVE, List.of(disguiseIdentifier), uuid));

        var future = new CompletableFuture<Boolean>();
        bindingSlave.getOrCreatePlayerFuture(uuid).thenAccept(ignored ->
        {
            var data = this.getOrCreatePlayerMeta(uuid);

            if (!data.getUnlockedDisguiseIdentifiers().contains(disguiseIdentifier))
                future.complete(true);
            else
                future.complete(false);
        });

        return future;
    }

    public @Nullable PlayerMeta nullablePlayerMeta(UUID uuid)
    {
        return localMetaMap.getOrDefault(uuid, null);
    }

    public @NotNull PlayerMeta getOrCreatePlayerMeta(UUID uuid)
    {
        var tracked = nullablePlayerMeta(uuid);
        if (tracked != null) return tracked;

        var metaInstance = new PlayerMeta();
        metaInstance.uniqueId = uuid;
        metaInstance.playerName = "No";

        localMetaMap.put(uuid, metaInstance);

        return metaInstance;
    }

    @Override
    public boolean reload()
    {
        dropAll();

        var players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                        .stream().map(Player::getUniqueId)
                        .toList();

        bindingSlave.requestData(players);

        return true;
    }

    public void dropAll()
    {
        logger.info("[Slave@NetworkData] Dropping cached network player meta...");

        this.localMetaMap.clear();
    }

    @Override
    public boolean save()
    {
        return true;
    }

}
