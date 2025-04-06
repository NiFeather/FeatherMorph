package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

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
        if (packet.getType() != WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP)
            return;

        var sourceEntityId = packet.getEntityId();
        var nmsPlayer = this.getNmsPlayerFrom(sourceEntityId);

        if (nmsPlayer == null) return;

        if (!(nmsPlayer.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        // Don't cancel for the source
        if (event.getPlayer().equals(sourcePlayer)) return;

        event.setCancelled(true);
    }
}
