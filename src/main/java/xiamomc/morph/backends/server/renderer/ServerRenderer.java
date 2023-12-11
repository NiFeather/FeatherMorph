package xiamomc.morph.backends.server.renderer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.MorphPacketListener;
import xiamomc.morph.backends.server.renderer.network.ProtocolListener;
import xiamomc.pluginbase.Managers.DependencyManager;

public class ServerRenderer extends MorphPluginObject
{
    private final ProtocolListener protocolListener = new ProtocolListener();

    public ServerRenderer()
    {
        var depMgr = DependencyManager.getManagerOrCreate(MorphPlugin.getInstance());
        depMgr.cache(protocolListener.getPacketListener());
    }

    public void renderEntity(Player player, EntityType entityType, String name)
    {
        var packetListener = protocolListener.getPacketListener();

        packetListener.register(player.getUniqueId(),
                new MorphPacketListener.RegistryParameters(entityType, name));
    }

    public void unRenderEntity(Player player)
    {
        var packetListener = protocolListener.getPacketListener();

        packetListener.unregister(player.getUniqueId());
    }

    public void dispose()
    {
        protocolListener.dispose();
    }
}
