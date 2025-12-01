package xyz.nifeather.morph.network.multiInstance.slave;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public List<DisguiseMeta> getAvaliableDisguisesFor(Player player)
    {
        var playerMeta = getPlayerMeta(player);

        return playerMeta.getUnlockedDisguises();
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        if (!bindingSlave.isOnline())
        {
            logger.error("We are not connected with master server! Refusing to update unlock state...");
            return false;
        }

        if (this.getPlayerMeta(player).getUnlockedDisguiseIdentifiers().stream().anyMatch(str -> str.equals(disguiseIdentifier)))
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

        if (this.getPlayerMeta(player).getUnlockedDisguiseIdentifiers().stream().noneMatch(str -> str.equals(disguiseIdentifier)))
            return false;

        bindingSlave.sendCommand(new MIC2SSyncDisguiseCommand(Operation.REMOVE, List.of(disguiseIdentifier), player.getUniqueId()));
        return true;
    }

    @Override
    public @NotNull PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        var uuid = player.getUniqueId();

        var tracked = localMetaMap.getOrDefault(uuid, null);
        if (tracked != null) return tracked;

        var metaInstance = new PlayerMeta();
        metaInstance.uniqueId = player.getUniqueId();
        metaInstance.playerName = player.getName();

        localMetaMap.put(uuid, metaInstance);

        return metaInstance;
    }

    @Override
    public boolean reloadConfiguration()
    {
        logger.info("[Slave@NetworkData] Dropping cached network player meta...");

        dropAll();

        var players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                        .stream().map(Player::getUniqueId)
                        .toList();

        bindingSlave.requestData(players);

        return true;
    }

    public void dropAll()
    {
        this.localMetaMap.clear();
    }

    @Override
    public boolean saveConfiguration()
    {
        return true;
    }

    @Override
    public void shouldLoadAllData(boolean shouldLoadAllData)
    {
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

    @Override
    public List<PlayerMeta> listAll()
    {
        return localMetaMap.values().stream().toList();
    }
}
