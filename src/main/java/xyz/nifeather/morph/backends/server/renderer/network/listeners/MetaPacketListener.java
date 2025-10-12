package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

/**
 * Listener used to override the metadata packet, so that the client won't panic when it received player's meta but the player is disguised as a mob.
 */
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
        var sourcePlayer = getPlayerFrom(packet.getEntityId());

        // How could this be?!
        if (sourcePlayer == null)
            return;

        if (sourcePlayer.equals(packetEvent.getPlayer())) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        //然后获取此包要发送的目标玩家
        var targetPlayer = packetEvent.getPlayer();

        //只拦截其他人的Meta
        if (targetPlayer.equals(sourcePlayer))
            return;

        var wrapper = new WrapperPlayServerEntityMetadata(packetEvent);

        try
        {
            watcher.handleEntityMetadataPacket(wrapper);
        }
        catch (Exception e)
        {
            boolean handled = false;
            var api = FeatherMorphAPI.instance();

            // Sometimes API would return NULL where I believe it shouldn't... D:
            if (api != null)
            {
                var state = api.directAccess().morphManager().getDisguiseStateFor(sourcePlayer);
                if (state != null)
                {
                    logger.info("Failed rebuilding server metadata packet, calling DisguiseState#handleException");
                    state.handleException(e);
                    handled = true;
                }
            }

            if (!handled)
            {
                // If API is not ready (where it shouldn't), unregister from render registry to prevent future chaos
                logger.error("Failed rebuilding server metadata packet", e);
                registry.unregister(watcher.bindingUUID);
            }
        }
    }
}
