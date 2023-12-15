package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.utilities.NmsUtils;
import xiamomc.pluginbase.Annotations.Resolved;

public class MetaPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public void onPacketSending(PacketEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        var packet = event.getPacket();

        //不要处理来自我们自己的包
        if (packet.getMeta(PacketFactory.MORPH_PACKET_METAKEY).isPresent())
            return;

        onMetaPacket((ClientboundSetEntityDataPacket) event.getPacket().getHandle(), event);
    }

    private void onMetaPacket(ClientboundSetEntityDataPacket packet, PacketEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = NmsUtils.getNmsLevel(packetEvent.getPlayer().getWorld()).getEntity(packet.id());
        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        //然后获取此包要发送的目标玩家
        var targetPlayer = packetEvent.getPlayer();

        //只拦截其他人的Meta
        if (targetPlayer == sourcePlayer)
            return;

        //不要二次处理来自我们自己的包
        var packetContainer = packetEvent.getPacket();
        var meta = packetContainer.getMeta(PacketFactory.MORPH_PACKET_METAKEY);
        if (meta.isEmpty())
        {
            //取得来源玩家的伪装后的Meta，发送给目标玩家
            packetEvent.setPacket(getFactory().buildFullMetaPacket(sourcePlayer, watcher));
        }
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

    @Override
    public org.bukkit.plugin.Plugin getPlugin()
    {
        return MorphPlugin.getInstance();
    }
}
