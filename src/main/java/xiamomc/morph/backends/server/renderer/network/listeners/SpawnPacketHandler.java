package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.skins.PlayerSkinProvider;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.ProtocolEquipment;
import xiamomc.morph.backends.server.renderer.network.RenderRegistry;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.*;

public class SpawnPacketHandler extends MorphPluginObject implements PacketListener, IProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    public SpawnPacketHandler()
    {
        registry.onRegister(this, ep ->
        {
            refreshStateForPlayer(ep.player());
        });

        registry.onUnRegister(this, this::unDisguiseForPlayer);
    }

    private List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        return sourcePlayer.getWorld().getPlayers();
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

        var meta = getMetaPackets(player, new PlayerWatcher(player, org.bukkit.entity.EntityType.PLAYER));
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
                        refreshStateForPlayer(player,
                                new DisplayParameters(outcomingProfile, disguiseName, registryParameters.bukkitType(), registryParameters.singleWatcher()));
                    }

                    return null;
                });
    }

    private List<PacketContainer> buildSpawnPackets(Player player, DisplayParameters parameters)
    {
        List<PacketContainer> packets = new ObjectArrayList<>();

        logger.info("Build spawn packets, player is " + player.getName() + " :: parameters are " + parameters);

        var playerType = parameters.bukkitType();
        var nmsType = EntityTypeUtils.getNmsType(playerType);
        if (nmsType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(playerType));

            addSchedule(() -> registry.unregister(player));
            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = player.getUniqueId();

        //如果是玩家
        if (playerType == org.bukkit.entity.EntityType.PLAYER)
        {
            logger.info("Building player info packet!");

            Objects.requireNonNull(parameters.gameProfile(), "Null game profile!");
            var gameProfile = new MorphGameProfile(parameters.gameProfile());

            //todo: Get random UUID from world
            //玩家在客户端的UUID会根据其GameProfile中的UUID设定，我们需要避免伪装的UUID和某一玩家自己的UUID冲突
            gameProfile.setUUID(UUID.randomUUID());

            //Minecraft需要在生成玩家实体前先发送PlayerInfoUpdate消息
            var uuid = gameProfile.getId();
            var packetPlayerInfo = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                    new ClientboundPlayerInfoUpdatePacket.Entry(
                            uuid, gameProfile, false, 114514, GameType.DEFAULT_MODE,
                            Component.literal(":>"), null
                    )
            );

            spawnUUID = uuid;
            packets.add(PacketContainer.fromPacket(packetPlayerInfo));
        }

        //生成实体
        var packetAdd = new ClientboundAddEntityPacket(
                player.getEntityId(), spawnUUID,
                player.getX(), player.getY(), player.getZ(),
                player.getPitch(), player.getYaw(),
                nmsType, 0,
                nmsPlayer.getDeltaMovement(),
                nmsPlayer.getYHeadRot()
        );

        var spawnPacket = PacketContainer.fromPacket(packetAdd);

        spawnPacket.setMeta("fm", true);
        packets.add(spawnPacket);

        var equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(player.getEquipment()));
        packets.add(PacketContainer.fromPacket(equipmentPacket));
        packets.add(getMetaPackets(player, parameters.watcher()));

        return packets;
    }

    private PacketContainer getMetaPackets(Player player, SingleWatcher watcher)
    {
        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        var entityWatcher = WrappedDataWatcher.getEntityWatcher(player);
        entityWatcher.asMap().forEach((id, val) ->
        {
            //logger.info("Id '%s' is val '%s' raw '%s' class '%s'"
            //        .formatted(id, val.getValue(),val.getRawValue(),val.getRawValue().getClass()));
        });

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();

        watcher.doSync();
        watcher.getRegistry().forEach((single, v) ->
        {
            if (single.defaultValue().equals(v)) return;

            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(single.defaultValue());
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(single.index(), t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(single.index(), serializer, v);
            wrappedDataValues.add(value);
        });

        modifier.write(0, wrappedDataValues);

        return metaPacket;
    }

    private void refreshStateForPlayer(@Nullable Player player)
    {
        if (player == null) return;

        var registryParameters = registry.getParameters(player.getUniqueId());
        if (registryParameters == null)
            throw new NullDependencyException("Null RegistryParameters for a existing player?!");

        refreshStateForPlayer(player, DisplayParameters.fromRegistry(registryParameters));
    }

    /**
     * 刷新玩家的伪装
     * @param player 目标玩家
     * @param parameters 如果伪装是玩家伪装，则为此伪装的GameProfile
     */
    private void refreshStateForPlayer(@Nullable Player player, @NotNull DisplayParameters parameters)
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

        var gameProfile = parameters.gameProfile();

        //然后发包创建实体
        //确保gameProfile非空
        //如果没有profile，那么随机一个并计划刷新
        if (displayType == org.bukkit.entity.EntityType.PLAYER)
        {
            if (gameProfile == null)
            {
                scheduleRefreshPlayerDisplay(player, parameters.playerDisguiseName());
                gameProfile = new GameProfile(Util.NIL_UUID, parameters.playerDisguiseName());
            }
        }

        var parametersFinal = new DisplayParameters(gameProfile, parameters.playerDisguiseName(), parameters.bukkitType(), parameters.watcher());
        var spawnPackets = buildSpawnPackets(player, parametersFinal);
        spawnPackets.forEach(packet ->
        {
            for (var visiblePlayer : affectedPlayers)
                protocolManager.sendServerPacket(visiblePlayer, packet);
        });
    }

    private void onEntityAddPacket(ClientboundAddEntityPacket packet, PacketEvent packetEvent)
    {
        var packetContainer = packetEvent.getPacket();
        var modifier = packetContainer.getModifier();

        var bindingParameters = registry.getParameters(packet.getUUID());
        if (bindingParameters == null)
        {
            return;
        }

        var entityType = EntityTypeUtils.getNmsType(bindingParameters.bukkitType());
        modifier.write(2, entityType);

        var meta = packetContainer.getMeta("fm");
        if (meta.isPresent()) packetContainer.removeMeta("fm");
        else refreshStateForPlayer(Bukkit.getPlayer(packet.getUUID()));
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

    @Override
    public Plugin getPlugin()
    {
        return MorphPlugin.getInstance();
    }
}
