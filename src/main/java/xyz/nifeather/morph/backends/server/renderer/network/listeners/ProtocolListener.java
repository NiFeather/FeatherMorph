package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;

public abstract class ProtocolListener extends MorphPluginObject implements PacketListener
{
    public abstract String getIdentifier();

    protected PlayerManager playerManager()
    {
        return PacketEvents.getAPI().getPlayerManager();
    }

    @Nullable
    protected Player getPlayerFrom(int id)
    {
        return featherMorph().getPlatform().onlinePlayersNative()
                .stream()
                .filter(p -> p.getEntityId() == id)
                .findFirst()
                .orElse(null);
    }
}
