package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.backends.server.ServerBackend;
import xyz.nifeather.morph.backends.server.renderer.network.DisplayParameters;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;

import java.util.List;
import java.util.UUID;

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

        //忽略不在注册表中的玩家
        var bindingWatcher = registry.getWatcher(uuid);
        if (bindingWatcher == null)
            return;

        // todo: 不要二次处理来自我们自己的包
        if (uuid.equals(bindingWatcher.readEntry(CustomEntries.SPAWN_UUID)))
            return;

        packetEvent.setCancelled(true);
        Player pl = packetEvent.getPlayer();

        try
        {
            ServerBackend.getInstance().serverRenderer.refreshStateForPlayer(Bukkit.getPlayer(uuid), List.of(pl));
        }
        catch (Throwable t)
        {
            logger.error("Failed to spawn fake entity: " + t.getMessage());
        }
    }
}
