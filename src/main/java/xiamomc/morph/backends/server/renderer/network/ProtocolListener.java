package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.ProtocolLibrary;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

public class ProtocolListener extends MorphPluginObject
{
    private final MorphPacketListener morphPacketListener = new MorphPacketListener();

    public MorphPacketListener getPacketListener()
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

        morphPacketListener.reset();

        disposed = true;
    }
}
