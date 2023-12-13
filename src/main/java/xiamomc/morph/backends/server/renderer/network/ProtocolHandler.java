package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.ProtocolLibrary;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.listeners.MetaPacketListener;
import xiamomc.morph.backends.server.renderer.network.listeners.SpawnPacketHandler;
import xiamomc.pluginbase.Annotations.Initializer;

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
        protocolMgr.addPacketListener(new MetaPacketListener());
        //protocolMgr.addPacketListener(new TestPacketListener());
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
