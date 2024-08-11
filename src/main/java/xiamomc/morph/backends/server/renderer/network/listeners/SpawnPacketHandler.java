package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.utilties.PacketUtils;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.misc.skins.PlayerSkinProvider;
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
                refreshStateForPlayer(ep.player(), getAffectedPlayers(ep.player())));

        registry.onUnRegister(this, ep ->
                unDisguiseForPlayer(ep.player(), ep.parameters()));
    }

    private List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        return WatcherUtils.getAffectedPlayers(sourcePlayer);
    }

    private void unDisguiseForPlayer(@Nullable Player player, SingleWatcher disguiseWatcher)
    {
        if (player == null) return;

        var playerManager = PacketEvents.getAPI().getPlayerManager();
        var affectedPlayers = getAffectedPlayers(player);
        var gameProfile = ((CraftPlayer) player).getProfile();
        var watcher = new PlayerWatcher(player);

        var parameters = new DisplayParameters(org.bukkit.entity.EntityType.PLAYER, watcher, gameProfile)
                .setDontRandomProfileUUID();

        var spawnPackets = getFactory().buildSpawnPackets(player, parameters);

        var removePacket = new WrapperPlayServerDestroyEntities(player.getEntityId());

        // 如果此伪装在TAB里留有一个Entry，那么移除它。
        var lastUUID = disguiseWatcher.getOrDefault(EntryIndex.TABLIST_UUID, null);
        if (lastUUID != null)
            spawnPackets.add(new WrapperPlayServerPlayerInfoRemove(List.of(lastUUID)));

        watcher.dispose();

        affectedPlayers.forEach(p ->
        {
            playerManager.sendPacket(p, removePacket);

            for (PacketWrapper<?> packet : spawnPackets)
                playerManager.sendPacket(p, packet);
        });
    }

    private void refreshStateForPlayer(@Nullable Player player, List<Player> affectedPlayers)
    {
        if (player == null) return;

        var watcher = registry.getWatcher(player.getUniqueId());
        if (watcher == null)
            throw new NullDependencyException("Null Watcher for a existing player?!");

        refreshStateForPlayer(player,
                new DisplayParameters(watcher.getEntityType(), watcher, watcher.get(EntryIndex.PROFILE)),
                affectedPlayers);
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
        var displayType = watcher.getEntityType();

        var protocolManager = PacketEvents.getAPI().getPlayerManager();

        //先发包移除当前实体
        var packetRemove = new WrapperPlayServerDestroyEntities(player.getEntityId());

        var gameProfile = watcher.get(EntryIndex.PROFILE);

        //然后发包创建实体
        //确保gameProfile非空
        //如果没有profile，那么随机一个并计划刷新
        if (displayType == org.bukkit.entity.EntityType.PLAYER && gameProfile == null)
        {
            var disguiseName = watcher.get(EntryIndex.DISGUISE_NAME);

            if (disguiseName == null || disguiseName.isBlank())
            {
                logger.error("Parameter 'disguiseName' cannot be null or blank!");
                Thread.dumpStack();
                return;
            }

            var targetPlayer = Bukkit.getPlayerExact(disguiseName);

            GameProfile targetProfile = watcher.getOrDefault(EntryIndex.PROFILE, null);

            if (targetProfile == null)
            {
                //皮肤在其他地方（例如PlayerDisguiseProvider#makeWrapper）中有做获取处理
                //因此这里只根据情况从缓存或者找到的玩家获取皮肤
                targetProfile = targetPlayer == null
                        ? PlayerSkinProvider.getInstance().getCachedProfile(disguiseName)
                        : NmsRecord.ofPlayer(targetPlayer).gameProfile;
            }

            gameProfile = Objects.requireNonNullElseGet(targetProfile, () -> new GameProfile(UUID.randomUUID(), disguiseName));
        }

        var parametersFinal = new DisplayParameters(displayType, watcher, gameProfile); //.setDontIncludeMeta();
        var spawnPackets = getFactory().buildSpawnPackets(player, parametersFinal);

        affectedPlayers.forEach(p ->
        {
            protocolManager.sendPacket(p, packetRemove);

            spawnPackets.forEach(packet -> protocolManager.sendPacket(p, packet));
        });
    }

    private void onEntityAddPacket(WrapperPlayServerSpawnEntity packet, PacketSendEvent packetEvent)
    {
        // 上下文：在 onPacketSending 中，我们过滤掉了非玩家的生成包
        var uuid = packet.getUUID().orElse(null);

        if (uuid == null)
        {
            logger.warn("Null UUID for a spawn packet?!");
            return;
        }

        //忽略不在注册表中的玩家
        var bindingWatcher = registry.getWatcher(uuid);
        if (bindingWatcher == null)
            return;

        //不要二次处理来自我们自己的包
        packetEvent.setCancelled(true);
        refreshStateForPlayer(Bukkit.getPlayer(packet.getUUID().orElseThrow()), List.of((Player) packetEvent.getPlayer()));
    }

    public void onPacketSending(PacketSendEvent packetEvent)
    {
        if (packetEvent.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY) return;

        var packet = new WrapperPlayServerSpawnEntity(packetEvent);
        if (packet.getEntityType() != EntityTypes.PLAYER) return;

        if (PacketUtils.isPacketOurs(packet))
        {
            PacketUtils.removeMark(packet);
            return;
        }

        // TODO: We need to change how this works.
        this.onEntityAddPacket(packet, packetEvent);
    }
}
