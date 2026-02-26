package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

/**
 * Listener used to override the entity animation packet so that we can keep the animation set by disguise action
 */
public class AnimationPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "animation_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_ANIMATION)
            return;

        var wrapper = new WrapperPlayServerEntityAnimation(event);
        this.onAnimationPacket(event, wrapper);
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    private void onAnimationPacket(PacketSendEvent event, WrapperPlayServerEntityAnimation packet)
    {
        var sourceEntityId = packet.getEntityId();
        var sourcePlayer = this.getPlayerFrom(sourceEntityId);

        if (sourcePlayer == null) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        if (!watcher.haveAnimation(packet.getType()))
        {
            event.setCancelled(true);
            return;
        }

        // Don't cancel for the source
        if (event.getPlayer().equals(sourcePlayer)) return;

        event.setCancelled(true);
    }
}
