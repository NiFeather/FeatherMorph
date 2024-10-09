package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.pluginbase.Annotations.Resolved;

public class AnimationPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "animation_listener";
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent)
    {
        if (packetEvent.getPacketType() != PacketType.Play.Server.ANIMATION)
        {
            logger.error("Unmatched packettype: " + packetEvent.getPacketType());
            return;
        }

        if (!(packetEvent.getPacket().getHandle() instanceof ClientboundAnimatePacket clientboundAnimatePacket))
        {
            logger.error("Handle is " + packetEvent.getPacket().getHandle() + ", But expect ClientboundAnimatePacket.");
            return;
        }

        onAnimationPacket(packetEvent, clientboundAnimatePacket);
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    private void onAnimationPacket(PacketEvent event, ClientboundAnimatePacket clientboundAnimatePacket)
    {
        if (clientboundAnimatePacket.getAction() != ClientboundAnimatePacket.WAKE_UP)
            return;

        var sourceEntityId = clientboundAnimatePacket.getId();
        var nmsPlayer = this.getNmsPlayerEntityFrom(event, sourceEntityId);

        if (nmsPlayer == null) return;

        if (!(nmsPlayer.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        // Don't cancel for the source
        if (event.getPlayer().equals(sourcePlayer)) return;

        event.setCancelled(true);
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
                .types(PacketType.Play.Server.ANIMATION)
                .gamePhase(GamePhase.PLAYING)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }
}
