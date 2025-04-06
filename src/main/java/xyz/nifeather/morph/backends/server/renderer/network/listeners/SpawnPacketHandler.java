package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.backends.server.renderer.network.DisplayParameters;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;

import java.util.List;
import java.util.UUID;

public class SpawnPacketHandler extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public String getIdentifier()
    {
        return "spawn_listener";
    }

    public SpawnPacketHandler()
    {
        registry.onRegister(this, ep ->
                refreshStateForPlayer(ep.player(), getAffectedPlayers(ep.player())));

        registry.onUnRegister(this, ep ->
                unDisguiseForPlayer(ep.player(), ep.watcher()));
    }

    private List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        return WatcherUtils.getAffectedPlayers(sourcePlayer);
    }

    private void unDisguiseForPlayer(@Nullable Player player, SingleWatcher disguiseWatcher)
    {
        if (player == null) return;

        var protocolManager = playerManager();
        var affectedPlayers = getAffectedPlayers(player);
        var watcher = new PlayerWatcher(player);
        watcher.markSilent(this);

        watcher.writeEntry(CustomEntries.PROFILE, ((CraftPlayer) player).getProfile());
        watcher.writeEntry(CustomEntries.SPAWN_UUID, player.getUniqueId());
        watcher.writeEntry(CustomEntries.SPAWN_ID, player.getEntityId());
        watcher.writeEntry(CustomEntries.PROFILE_LISTED, true);
        watcher.writeEntry(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, true);

        var packets = watcher.buildSpawnPackets();

        var removePacket = new WrapperPlayServerDestroyEntities(player.getEntityId());

        if (disguiseWatcher.getEntityType() == org.bukkit.entity.EntityType.PLAYER
                && !disguiseWatcher.readEntryOrDefault(CustomEntries.PROFILE_LISTED, false))
        {
            var disguiseUUID = disguiseWatcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);

            var packetRemoveInfo = new WrapperPlayServerPlayerInfoRemove(disguiseUUID);

            Bukkit.getOnlinePlayers().forEach(p -> protocolManager.sendPacket(p, packetRemoveInfo));
        }

        watcher.dispose();

        affectedPlayers.forEach(p ->
        {
            protocolManager.sendPacket(p, removePacket);

            for (var packet : packets)
                protocolManager.sendPacket(p, packet);
        });
    }

    private void refreshStateForPlayer(@Nullable Player player, List<Player> affectedPlayers)
    {
        if (player == null) return;

        var watcher = registry.getWatcher(player.getUniqueId());
        if (watcher == null)
            throw new NullDependencyException("Null Watcher for a existing player?!");

        refreshStateForPlayer(player,
                new DisplayParameters(watcher),
                affectedPlayers);
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY)
            return;

        var wrapper = new WrapperPlayServerSpawnEntity(event);
        this.onEntityAddPacket(wrapper, event);
    }

    /**
     * 刷新玩家的伪装
     * @param player 目标玩家
     * @param displayParameters 和伪装对应的 {@link DisplayParameters}
     */
    private void refreshStateForPlayer(@Nullable Player player, @NotNull DisplayParameters displayParameters, List<Player> affectedPlayers)
    {
        if (affectedPlayers.isEmpty()) return;

        if (player == null) return;
        var watcher = displayParameters.getWatcher();

        var protocolManager = playerManager();

        //先发包移除当前实体
        var removePacket = new WrapperPlayServerDestroyEntities(player.getEntityId());

        //然后发包创建实体
        //确保gameProfile非空
        //如果没有profile，那么随机一个并计划刷新
        if (watcher.getEntityType() == org.bukkit.entity.EntityType.PLAYER && watcher.readEntry(CustomEntries.PROFILE) == null)
        {
            var disguiseName = watcher.readEntry(CustomEntries.DISGUISE_NAME);

            if (disguiseName == null || disguiseName.isBlank())
            {
                logger.error("Parameter 'disguiseName' cannot be null or blank!");
                Thread.dumpStack();
                return;
            }

            var targetPlayer = Bukkit.getPlayerExact(disguiseName);

            GameProfile targetProfile = watcher.readEntryOrDefault(CustomEntries.PROFILE, null);

            if (targetProfile == null)
            {
                //皮肤在其他地方（例如PlayerDisguiseProvider#makeWrapper）中有做获取处理
                //因此这里只根据情况从缓存或者找到的玩家获取皮肤
                targetProfile = targetPlayer == null
                        ? PlayerSkinProvider.getInstance().getCachedProfile(disguiseName)
                        : NmsRecord.ofPlayer(targetPlayer).gameProfile;
            }

            watcher.writeEntry(CustomEntries.PROFILE, targetProfile == null ? new GameProfile(UUID.randomUUID(), disguiseName) : targetProfile);
        }

        var spawnPackets = watcher.buildSpawnPackets();

        affectedPlayers.forEach(p ->
        {
            protocolManager.sendPacket(p, removePacket);

            spawnPackets.forEach(packet -> protocolManager.sendPacket(p, packet));
        });
    }

    private void onEntityAddPacket(WrapperPlayServerSpawnEntity packet, PacketSendEvent packetEvent)
    {
        var uuid = packet.getUUID().orElse(null);

        if (uuid == null)
            return;

        //忽略不在注册表中的玩家
        var bindingWatcher = registry.getWatcher(uuid);
        if (bindingWatcher == null)
            return;

        logger.info("Capture!");

        // todo: 不要二次处理来自我们自己的包
        if (uuid.equals(bindingWatcher.readEntry(CustomEntries.SPAWN_UUID)))
        {
            logger.info("Skipping packet that is possible ours!");
            return;
        }

        packetEvent.setCancelled(true);
        Player pl = packetEvent.getPlayer();
        refreshStateForPlayer(Bukkit.getPlayer(uuid), List.of(pl));
    }
}
