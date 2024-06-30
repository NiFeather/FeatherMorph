package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;

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
            logger.info("Unmatched packettype: " + packetEvent.getPacketType());
            return;
        }

        if (!(packetEvent.getPacket().getHandle() instanceof ClientboundAnimatePacket clientboundAnimatePacket))
        {
            logger.info("Handle is " + packetEvent.getPacket().getHandle() + ", But expect ClientboundAnimatePacket.");
            return;
        }

        onAnimationPacket(packetEvent, clientboundAnimatePacket);
    }

    private void onAnimationPacket(PacketEvent event, ClientboundAnimatePacket clientboundAnimatePacket)
    {
        if (clientboundAnimatePacket.getAction() == ClientboundAnimatePacket.WAKE_UP)
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
