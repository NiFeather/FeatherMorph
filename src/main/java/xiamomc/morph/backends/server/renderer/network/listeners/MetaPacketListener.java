package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.backends.server.renderer.utilties.PacketUtils;
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


    public void onPacketSending(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        var wrapper = new WrapperPlayServerEntityMetadata(event);

        if (PacketUtils.isPacketOurs(wrapper))
        {
            PacketUtils.removeMark(wrapper);
            return;
        }

        onMetaPacket(wrapper, event);
    }

    private void onMetaPacket(WrapperPlayServerEntityMetadata packet, PacketSendEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerEntityFrom(packet.getEntityId());

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

        // 不要处理同为玩家的Meta包
        // 如果不是玩家伪装，则从包里移除玩家meta中不属于BASE_LIVING的部分
        if (watcher.getEntityType() != EntityType.PLAYER)
            packet.setEntityMetadata(getFactory().removeNonLivingValues(ValueIndex.BASE_LIVING, packet.getEntityMetadata()));
    }
}
