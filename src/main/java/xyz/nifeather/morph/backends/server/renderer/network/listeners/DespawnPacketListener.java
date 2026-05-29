package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.ServerBackend;

import java.util.Objects;

public class DespawnPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "despawn_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.DESTROY_ENTITIES)
            return;

        Player receiver = event.getPlayer();
        var wrapper = new WrapperPlayServerDestroyEntities(event);
        var world = receiver.getWorld();
        var registry = Objects.requireNonNull(ServerBackend.getInstance())
                .serverRenderer
                .registry;

        for (int entityId : wrapper.getEntityIds())
        {
            var entity = SpigotConversionUtil.getEntityById(world, entityId);
            if (entity == null) continue;

            var watcher = registry.getWatcher(entity.getUniqueId());
            if (watcher == null || watcher.disposed()) continue;

            watcher.onEntityDestroy(receiver);
        }
    }
}
