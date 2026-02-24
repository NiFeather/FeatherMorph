package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.ServerBackend;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.EntityWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.misc.BuildFailedException;

import java.util.List;

/**
 * The listener that handles the spawn packet!
 * We also handle renderer register/unregister here. todo: Maybe we want to move this part to somewhere else?
 */
public class SpawnPacketHandler extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public String getIdentifier()
    {
        return "spawn_listener";
    }

    public SpawnPacketHandler()
    {
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY)
            return;

        var wrapper = new WrapperPlayServerSpawnEntity(event);
        this.onEntityAddPacket(wrapper, event);
    }

    private void onEntityAddPacket(WrapperPlayServerSpawnEntity packet, PacketSendEvent packetEvent)
    {
        var uuid = packet.getUUID().orElse(null);

        if (uuid == null)
            return;

        // Ignore players that's not in the registry
        var bindingWatcher = registry.getWatcher(uuid);
        if (bindingWatcher == null)
            return;

        var backend = ServerBackend.getInstance();
        if (backend == null) return;

        Player affectedPlayer = packetEvent.getPlayer();
        if (backend.serverRenderer.scheduleDisguise(bindingWatcher, List.of(affectedPlayer)))
            packetEvent.setCancelled(true);
    }
}
