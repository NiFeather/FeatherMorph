package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryParameters;
import xiamomc.morph.backends.server.renderer.skins.PlayerSkinProvider;
import xiamomc.morph.backends.server.renderer.network.ProtocolEquipment;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.*;

public class SpawnPacketHandler extends ProtocolListener implements PacketListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    public SpawnPacketHandler()
    {
        registry.onRegister(this, ep ->
                refreshStateForPlayer(ep.player()));

        registry.onUnRegister(this, this::unDisguiseForPlayer);
    }

    private List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        var players = sourcePlayer.getWorld().getPlayers();
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
        affectedPlayers.remove(player);

        var removePacket = new ClientboundRemoveEntitiesPacket(player.getEntityId());
        var rmPacketContainer = PacketContainer.fromPacket(removePacket);
        affectedPlayers.forEach(p -> protocolManager.sendServerPacket(p, rmPacketContainer));

        var gameProfile = ((CraftPlayer) player).getProfile();
        var nmsPlayer = NmsRecord.ofPlayer(player);

        List<PacketContainer> packets = new ObjectArrayList<>();

        var packetPlayerInfo = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                new ClientboundPlayerInfoUpdatePacket.Entry(
                        player.getUniqueId(), gameProfile, false, 114514, GameType.DEFAULT_MODE,
                        Component.empty(), null
                )
        );

        var packetAdd = new ClientboundAddEntityPacket(
                player.getEntityId(), player.getUniqueId(),
                player.getX(), player.getY(), player.getZ(),
                player.getPitch(), player.getYaw(),
                EntityType.PLAYER, 0,
                nmsPlayer.getDeltaMovement(),
                nmsPlayer.getYHeadRot()
        );

        packets.add(PacketContainer.fromPacket(packetPlayerInfo));
        packets.add(PacketContainer.fromPacket(packetAdd));

        var equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(player.getEquipment()));
        packets.add(PacketContainer.fromPacket(equipmentPacket));

        var meta = getFactory().buildMetaPacket(player, new PlayerWatcher(player));
        packets.add(meta);

        affectedPlayers.forEach(p ->
        {
            for (PacketContainer packet : packets)
            {
                protocolManager.sendServerPacket(p, packet);
            }
        });
    }

    /**
     * 获取玩家伪装的皮肤，并刷新到玩家上
     * @param player 目标玩家
     * @param disguiseName 伪装的玩家名称
     */
    private void scheduleRefreshPlayerDisplay(Player player, String disguiseName)
    {
        PlayerSkinProvider.getInstance().fetchSkin(disguiseName)
                .thenApply(optional ->
                {
                    GameProfile outcomingProfile = new GameProfile(UUID.randomUUID(), disguiseName);
                    if (optional.isPresent()) outcomingProfile = optional.get();

                    var registryParameters = registry.getParameters(player.getUniqueId());

                    if (registryParameters != null)
                    {
                        logger.info("Triggering refresh!");
                        GameProfile finalOutcomingProfile = outcomingProfile;
                        this.addSchedule(() -> refreshStateForPlayer(player,
                                new RegistryParameters(registryParameters.bukkitType(), disguiseName, registryParameters.watcher(), finalOutcomingProfile)));
                    }

                    return null;
                });
    }

    private void refreshStateForPlayer(@Nullable Player player)
    {
        if (player == null) return;

        var registryParameters = registry.getParameters(player.getUniqueId());
        if (registryParameters == null)
            throw new NullDependencyException("Null RegistryParameters for a existing player?!");

        refreshStateForPlayer(player, registryParameters);
    }

    /**
     * 刷新玩家的伪装
     * @param player 目标玩家
     * @param parameters 如果伪装是玩家伪装，则为此伪装的GameProfile
     */
    private void refreshStateForPlayer(@Nullable Player player, @NotNull RegistryParameters parameters)
    {
        if (player == null) return;
        var displayType = parameters.bukkitType();

        var protocolManager = ProtocolLibrary.getProtocolManager();
        var affectedPlayers = getAffectedPlayers(player);
        affectedPlayers.remove(player);

        //先发包移除当前实体
        var packetRemove = new ClientboundRemoveEntitiesPacket(player.getEntityId());
        var packetRemoveContainer = PacketContainer.fromPacket(packetRemove);

        affectedPlayers.forEach(p -> protocolManager.sendServerPacket(p, packetRemoveContainer));

        var gameProfile = parameters.getProfile();

        //然后发包创建实体
        //确保gameProfile非空
        //如果没有profile，那么随机一个并计划刷新
        if (displayType == org.bukkit.entity.EntityType.PLAYER && gameProfile == null)
        {
            var targetPlayer = Bukkit.getPlayerExact(parameters.disguiseName());

            var cachedProfile = targetPlayer == null
                    ? PlayerSkinProvider.getInstance().getCachedProfile(parameters.disguiseName())
                    : NmsRecord.ofPlayer(targetPlayer).gameProfile;

            if (cachedProfile == null)
            {
                scheduleRefreshPlayerDisplay(player, parameters.disguiseName());
                gameProfile = new GameProfile(Util.NIL_UUID, parameters.disguiseName());
            }
            else
                gameProfile = cachedProfile;
        }

        var parametersFinal = new RegistryParameters(parameters.bukkitType(), parameters.disguiseName(), parameters.watcher(), gameProfile);
        var spawnPackets = getFactory().buildSpawnPackets(player, parametersFinal);
        spawnPackets.forEach(packet ->
        {
            for (var visiblePlayer : affectedPlayers)
                protocolManager.sendServerPacket(visiblePlayer, packet);
        });
    }

    private void onEntityAddPacket(ClientboundAddEntityPacket packet, PacketEvent packetEvent)
    {
        var packetContainer = packetEvent.getPacket();

        //忽略不在注册表中的玩家
        var bindingParameters = registry.getParameters(packet.getUUID());
        if (bindingParameters == null)
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
                .types(PacketType.Play.Server.ENTITY_METADATA)
                .types(PacketType.Play.Server.SPAWN_ENTITY)
                .gamePhase(GamePhase.PLAYING)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public Plugin getPlugin()
    {
        return MorphPlugin.getInstance();
    }
}
