package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.misc.skins.PlayerSkinProvider;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.List;
import java.util.Objects;
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
                refreshStateForPlayer(ep.player()));

        registry.onUnRegister(this, this::unDisguiseForPlayer);
    }

    private List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        var players = sourcePlayer.getWorld().getPlayers();
        players.remove(sourcePlayer);
        if (NmsRecord.ofPlayer(sourcePlayer).gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
        {
            players.removeIf(bukkitPlayer ->
                    NmsRecord.ofPlayer(bukkitPlayer).gameMode.getGameModeForPlayer() != GameType.SPECTATOR);
        }

        return players;
    }

    private void unDisguiseForPlayer(@Nullable Player player)
    {
        if (player == null) return;

        var protocolManager = ProtocolLibrary.getProtocolManager();
        var affectedPlayers = getAffectedPlayers(player);
        var gameProfile = ((CraftPlayer) player).getProfile();
        var watcher = new PlayerWatcher(player);

        var parameters = new DisplayParameters(org.bukkit.entity.EntityType.PLAYER, watcher, gameProfile)
                .setDontRandomProfileUUID();

        var spawnPackets = getFactory().buildSpawnPackets(player, parameters);

        var removePacket = new ClientboundRemoveEntitiesPacket(player.getEntityId());
        var rmPacketContainer = PacketContainer.fromPacket(removePacket);
        affectedPlayers.forEach(p ->
        {
            protocolManager.sendServerPacket(p, rmPacketContainer);

            for (PacketContainer packet : spawnPackets)
                protocolManager.sendServerPacket(p, packet);
        });
    }

    private void refreshStateForPlayer(@Nullable Player player)
    {
        if (player == null) return;

        var watcher = registry.getWatcher(player.getUniqueId());
        if (watcher == null)
            throw new NullDependencyException("Null Watcher for a existing player?!");

        refreshStateForPlayer(player, new DisplayParameters(watcher.getEntityType(), watcher, watcher.get(EntryIndex.PROFILE)));
    }

    /**
     * 刷新玩家的伪装
     * @param player 目标玩家
     * @param displayParameters 和伪装对应的 {@link DisplayParameters}
     */
    private void refreshStateForPlayer(@Nullable Player player, @NotNull DisplayParameters displayParameters)
    {
        if (player == null) return;
        var watcher = displayParameters.getWatcher();
        var displayType = watcher.getEntityType();

        var protocolManager = ProtocolLibrary.getProtocolManager();
        var affectedPlayers = getAffectedPlayers(player);

        //先发包移除当前实体
        var packetRemove = new ClientboundRemoveEntitiesPacket(player.getEntityId());
        var packetRemoveContainer = PacketContainer.fromPacket(packetRemove);

        var gameProfile = watcher.get(EntryIndex.PROFILE);

        //然后发包创建实体
        //确保gameProfile非空
        //如果没有profile，那么随机一个并计划刷新
        if (displayType == org.bukkit.entity.EntityType.PLAYER && gameProfile == null)
        {
            var disguiseName = watcher.get(EntryIndex.DISGUISE_NAME);

            if (disguiseName == null)
            {
                logger.error("Parameter 'disguiseName' cannot be null!");
                Thread.dumpStack();
                return;
            }

            var targetPlayer = Bukkit.getPlayerExact(disguiseName);

            var cachedProfile = targetPlayer == null
                    ? PlayerSkinProvider.getInstance().getCachedProfile(disguiseName)
                    : NmsRecord.ofPlayer(targetPlayer).gameProfile;

            gameProfile = Objects.requireNonNullElseGet(cachedProfile, () -> new GameProfile(UUID.randomUUID(), disguiseName));
        }

        var parametersFinal = new DisplayParameters(displayType, watcher, gameProfile).setDontIncludeMeta();
        var spawnPackets = getFactory().buildSpawnPackets(player, parametersFinal);

        affectedPlayers.forEach(p ->
        {
            protocolManager.sendServerPacket(p, packetRemoveContainer);

            spawnPackets.forEach(packet -> protocolManager.sendServerPacket(p, packet));
        });
    }

    private void onEntityAddPacket(ClientboundAddEntityPacket packet, PacketEvent packetEvent)
    {
        var packetContainer = packetEvent.getPacket();

        //忽略不在注册表中的玩家
        var bindingWatcher = registry.getWatcher(packet.getUUID());
        if (bindingWatcher == null)
            return;

        //不要二次处理来自我们自己的包
        var meta = packetContainer.getMeta(PacketFactory.MORPH_PACKET_METAKEY);
        if (meta.isEmpty())
        {
            packetEvent.setCancelled(true);
            refreshStateForPlayer(Bukkit.getPlayer(packet.getUUID()));
        }
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent)
    {
        if (!packetEvent.isServerPacket()) return;

        var packetContainer = packetEvent.getPacket();
        if (packetContainer.getHandle() instanceof ClientboundAddEntityPacket originalPacket
                && originalPacket.getType() == EntityType.PLAYER)
        {
            onEntityAddPacket(originalPacket, packetEvent);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent)
    {
    }

    @Override
    public ListeningWhitelist getSendingWhitelist()
    {
        return ListeningWhitelist
                .newBuilder()
                .types(PacketType.Play.Server.SPAWN_ENTITY)
                .gamePhase(GamePhase.PLAYING)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }
}
