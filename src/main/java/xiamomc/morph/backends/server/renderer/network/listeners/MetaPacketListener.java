package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.pluginbase.Annotations.Resolved;

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
    public void onPacketSending(PacketEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        var packet = event.getPacket();

        //不要处理来自我们自己的包
        if (getFactory().isPacketOurs(packet))
        {
            //logger.info("Got our packet with hash " + packet.getHandle().hashCode());
            return;
        }

        onMetaPacket((ClientboundSetEntityDataPacket) event.getPacket().getHandle(), event);
    }

    private void onMetaPacket(ClientboundSetEntityDataPacket packet, PacketEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerEntityFrom(packetEvent, packet.id());

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
