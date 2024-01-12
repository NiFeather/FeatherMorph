package xiamomc.morph.backends.server.renderer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.ProtocolHandler;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.RegisterParameters;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;

public class ServerRenderer extends MorphPluginObject
{
    private final ProtocolHandler protocolHandler;

    public final RenderRegistry registry = new RenderRegistry();

    private final PacketFactory packetFactory = new PacketFactory();

    public ServerRenderer()
    {
        dependencies.cache(packetFactory);

        dependencies.cache(registry);
        dependencies.cache(protocolHandler = new ProtocolHandler());
    }

    /**
     * 向后端渲染器注册玩家
     * @param player 目标玩家
     * @param entityType 目标类型
     * @param name 伪装名称
     */
    public SingleWatcher registerEntity(Player player, EntityType entityType, String name)
    {
        try
        {
            return registry.register(player, new RegisterParameters(entityType, name));
        }
        catch (Throwable t)
        {
            logger.error("Can't register player: " + t.getMessage());
            t.printStackTrace();

            unRegisterEntity(player);
        }

        return null;
    }

    public void unRegisterEntity(Player player)
    {
        try
        {
            registry.unregister(player.getUniqueId());
        }
        catch (Throwable t)
        {
            logger.info("Can't unregister player: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();
    }
}
