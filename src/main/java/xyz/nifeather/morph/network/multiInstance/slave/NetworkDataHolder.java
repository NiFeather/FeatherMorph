package xyz.nifeather.morph.network.multiInstance.slave;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
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

public class NetworkDataHolder extends MorphPluginObject implements IManagePlayerData
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
    public List<DisguiseMeta> getAvailableDisguisesFor(Player player)
    {
        var playerMeta = getPlayerMeta(player);

        return playerMeta.getUnlockedDisguises();
    }

    @Override
    public CompletableFuture<PlayerMeta> loadPlayerDataAsync(UUID uuid)
    {
        var future = new CompletableFuture<PlayerMeta>();

        bindingSlave.requestData(uuid).thenAccept(u ->
        {
            var data = nullablePlayerMeta(Bukkit.getOfflinePlayer(u));

            if (data != null)
                future.complete(data);
            else
                future.completeExceptionally(new NullDependencyException("The future of the requested data has been finished, but we can't find a matching data in NetworkDataHolder!"));
        });

        return future;
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        if (!bindingSlave.isOnline())
        {
            logger.error("We are not connected with master server! Refusing to update unlock state...");
            return false;
        }

        // Don't continue if we don't have got the required metadata
        var meta = nullablePlayerMeta(player);
        if (meta == null)
            return false;

        if (meta.getUnlockedDisguiseIdentifiers().stream().anyMatch(str -> str.equals(disguiseIdentifier)))
            return false;

        bindingSlave.sendCommand(new MIC2SSyncDisguiseCommand(Operation.ADD_IF_ABSENT, List.of(disguiseIdentifier), player.getUniqueId()));
        return true;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        if (!bindingSlave.isOnline())
        {
            logger.error("We are not connected with master server! Refusing to update unlock state...");
            return false;
        }

        // Don't continue if we don't have got the required metadata
        var meta = nullablePlayerMeta(player);
        if (meta == null)
            return false;

        if (meta.getUnlockedDisguiseIdentifiers().stream().noneMatch(str -> str.equals(disguiseIdentifier)))
            return false;

        bindingSlave.sendCommand(new MIC2SSyncDisguiseCommand(Operation.REMOVE, List.of(disguiseIdentifier), player.getUniqueId()));
        return true;
    }

    public @Nullable PlayerMeta nullablePlayerMeta(OfflinePlayer player)
    {
        return localMetaMap.getOrDefault(player.getUniqueId(), null);
    }

    public @NotNull PlayerMeta getOrCreatePlayerMeta(OfflinePlayer player)
    {
        var tracked = nullablePlayerMeta(player);
        if (tracked != null) return tracked;

        var metaInstance = new PlayerMeta();
        metaInstance.uniqueId = player.getUniqueId();
        metaInstance.playerName = player.getName();

        localMetaMap.put(player.getUniqueId(), metaInstance);

        return metaInstance;
    }

    @Override
    public @NotNull PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        var tracked = nullablePlayerMeta(player);
        if (tracked != null) return tracked;

        //todo: I don't know if this is good
        var tempInstance = new PlayerMeta();
        tempInstance.uniqueId = player.getUniqueId();
        tempInstance.playerName = player.getName();

        return tempInstance;
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

    public void drop(UUID uuid)
    {
        localMetaMap.remove(uuid);
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

    @Override
    public List<PlayerMeta> getRange(List<UUID> list)
    {
        List<PlayerMeta> metaList = new ObjectArrayList<>();

        list.forEach(uuid ->
        {
            var existing = localMetaMap.getOrDefault(uuid, null);
            if (existing != null) metaList.add(existing);
        });

        return metaList;
    }

}
