package xiamomc.morph.backends.server.renderer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.*;
import xiamomc.morph.backends.server.renderer.network.datawatcher.WatcherIndex;
import xiamomc.morph.backends.server.renderer.network.queue.PacketQueue;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryParameters;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.skins.SkinStore;

public class ServerRenderer extends MorphPluginObject
{
    private final ProtocolHandler protocolHandler;

    public final RenderRegistry registry = new RenderRegistry();

    private final SkinStore skinStore = new SkinStore();

    private final PacketFactory packetFactory = new PacketFactory();

    private final PacketQueue packetQueue = new PacketQueue();

    public ServerRenderer()
    {
        dependencies.cache(packetFactory);
        dependencies.cache(packetQueue);

        dependencies.cache(registry);
        dependencies.cache(skinStore);
        dependencies.cache(protocolHandler = new ProtocolHandler());
    }

    /**
     * 向后端渲染器注册玩家
     * @param player 目标玩家
     * @param entityType 目标类型
     * @param name 伪装名称
     */
    public RegistryParameters registerEntity(Player player, EntityType entityType, String name)
    {
        var parameters = new RegistryParameters(
                entityType, name,
                WatcherIndex.getInstance().getWatcherForType(player, entityType),
                null);

        registry.register(player.getUniqueId(), parameters);
        return parameters;
    }

    public void unRegisterEntity(Player player)
    {
        registry.unregister(player.getUniqueId());
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();
    }
}
