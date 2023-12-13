package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.ProtocolLibrary;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.Watchers;
import xiamomc.morph.backends.server.renderer.network.listeners.SpawnPacketHandler;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.UUID;

public class ProtocolHandler extends MorphPluginObject
{
    private final SpawnPacketHandler morphPacketListener = new SpawnPacketHandler();

    public SpawnPacketHandler getPacketListener()
    {
        return morphPacketListener;
    }

    @Initializer
    private void load()
    {
        if (disposed) return;

        var protocolMgr = ProtocolLibrary.getProtocolManager();

        protocolMgr.addPacketListener(morphPacketListener);
    }

    private boolean disposed;

    public void dispose()
    {
        try
        {
            var protocolMgr = ProtocolLibrary.getProtocolManager();

            protocolMgr.removePacketListener(morphPacketListener);
        }
        catch (Throwable t)
        {
            logger.error("Error removing packet listener: " + t.getMessage());
            t.printStackTrace();
        }

        disposed = true;
    }
}
