package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.ProtocolLibrary;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.listeners.*;
import xiamomc.pluginbase.Annotations.Initializer;

public class ProtocolHandler extends MorphPluginObject
{
    private final SpawnPacketHandler morphPacketListener = new SpawnPacketHandler();
    private final MetaPacketListener metaPacketListener = new MetaPacketListener();
    private final EquipmentPacketListener equipmentPacketListener = new EquipmentPacketListener();
    private final PlayerLookPacketListener playerLookPacketListener = new PlayerLookPacketListener();

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
        protocolMgr.addPacketListener(metaPacketListener);
        protocolMgr.addPacketListener(equipmentPacketListener);
        protocolMgr.addPacketListener(playerLookPacketListener);
        //protocolMgr.addPacketListener(new TestPacketListener());
    }

    private boolean disposed;

    public void dispose()
    {
        try
        {
            var protocolMgr = ProtocolLibrary.getProtocolManager();

            protocolMgr.removePacketListener(morphPacketListener);
            protocolMgr.removePacketListener(metaPacketListener);
            protocolMgr.removePacketListener(equipmentPacketListener);
            protocolMgr.removePacketListener(playerLookPacketListener);
        }
        catch (Throwable t)
        {
            logger.error("Error removing packet listener: " + t.getMessage());
            t.printStackTrace();
        }

        disposed = true;
    }
}
