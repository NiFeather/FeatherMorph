package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.pluginbase.Annotations.Resolved;

public class AnimationPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "animation_listener";
    }

    public void onPacketSending(PacketSendEvent packetEvent)
    {
        if (packetEvent.getPacketType() != PacketType.Play.Server.ENTITY_ANIMATION)
            return;

        var packetWrapper = new WrapperPlayServerEntityAnimation(packetEvent);
        onAnimationPacket(packetEvent, packetWrapper);
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    private void onAnimationPacket(PacketSendEvent event, WrapperPlayServerEntityAnimation packetWrapper)
    {
        if (packetWrapper.getType() != WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP)
            return;

        var sourceEntityId = packetWrapper.getEntityId();
        var nmsPlayer = this.getNmsPlayerEntityFrom(sourceEntityId);

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
