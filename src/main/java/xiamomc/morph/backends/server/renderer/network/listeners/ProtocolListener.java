package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.queue.PacketQueue;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class ProtocolListener extends MorphPluginObject
{
    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Resolved(shouldSolveImmediately = true)
    private PacketQueue packetQueue;

    protected PacketQueue getQueue()
    {
        return packetQueue;
    }

    protected PacketFactory getFactory() { return packetFactory; }

    protected ProtocolManager protocolManager()
    {
        return ProtocolLibrary.getProtocolManager();
    }
}
