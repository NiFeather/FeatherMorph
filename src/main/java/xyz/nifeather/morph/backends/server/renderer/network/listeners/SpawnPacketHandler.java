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
        var backend = ServerBackend.getInstance();
        if (backend == null) return;

        var uuid = packet.getUUID().orElse(null);

        if (uuid == null)
            return;

        //忽略不在注册表中的玩家
        var bindingWatcher = registry.getWatcher(uuid);
        if (bindingWatcher == null)
            return;

        // todo: 不要二次处理来自我们自己的包
        if (packet.getData() == EntityWatcher.PACKET_MARK)
            return;

        try
        {
            var disguisedPlayer = Bukkit.getPlayer(uuid);
            if (disguisedPlayer != null)
            {
                Player affectedPlayer = packetEvent.getPlayer();
                backend.serverRenderer.refreshStateForPlayer(disguisedPlayer, List.of(affectedPlayer));
                packetEvent.setCancelled(true);
            }
        }
        catch (Throwable t)
        {
            var sourcePlayer = getPlayerFrom(packet.getEntityId());
            handleException(sourcePlayer, bindingWatcher, t);
        }
    }
}
