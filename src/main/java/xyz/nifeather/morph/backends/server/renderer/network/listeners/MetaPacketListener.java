package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class MetaPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public String getIdentifier()
    {
        return "meta_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        var wrapper = new WrapperPlayServerEntityMetadata(event);
        this.onMetaPacket(wrapper, event);
    }

    private void onMetaPacket(WrapperPlayServerEntityMetadata packet, PacketSendEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerFrom(packet.getEntityId());

        // How could this be?!
        if (sourceNmsEntity == null)
            return;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        if (sourcePlayer.equals(packetEvent.getPlayer())) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        //然后获取此包要发送的目标玩家
        var targetPlayer = packetEvent.getPlayer();

        //只拦截其他人的Meta
        if (targetPlayer == sourcePlayer)
            return;

        //不要二次处理来自我们自己的包
        //并且不要处理同为玩家的Meta包
        var packetContainer = packetEvent.getPacket();

        //取得来源玩家的伪装后的Meta，发送给目标玩家
        //从包里移除玩家meta中不属于BASE_LIVING的部分
        var isPlayerDisguise = watcher.getEntityType() == EntityType.PLAYER;
        var finalPacket = getFactory().rebuildServerMetaPacket(
                isPlayerDisguise ? ValueIndex.PLAYER : ValueIndex.BASE_LIVING,
                watcher,
                packetContainer);

        if (finalPacket.getDataValueCollectionModifier().size() == 0)
            packetEvent.setCancelled(true);

        packetEvent.setPacket(finalPacket);
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
    }

    @Override
    public ListeningWhitelist getSendingWhitelist()
    {
        return ListeningWhitelist
                .newBuilder()
                .types(PacketType.Play.Server.ENTITY_METADATA)
                .gamePhase(GamePhase.PLAYING)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }
}
