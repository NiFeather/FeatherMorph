package xiamomc.morph.backends.server.renderer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.RegistryParameters;
import xiamomc.morph.backends.server.renderer.network.RenderRegistry;
import xiamomc.morph.backends.server.renderer.network.listeners.SpawnPacketHandler;
import xiamomc.morph.backends.server.renderer.network.ProtocolHandler;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.Watchers;
import xiamomc.morph.backends.server.renderer.skins.SkinStore;
import xiamomc.pluginbase.Managers.DependencyManager;

public class ServerRenderer extends MorphPluginObject
{
    private final ProtocolHandler protocolHandler;

    private final RenderRegistry registry = new RenderRegistry();

    private final SkinStore skinStore = new SkinStore();

    public ServerRenderer()
    {
        dependencies.cache(registry);
        dependencies.cache(skinStore);
        dependencies.cache(protocolHandler = new ProtocolHandler());
    }

    public void renderEntity(Player player, EntityType entityType, String name)
    {
        registry.register(player.getUniqueId(),
                new RegistryParameters(entityType, name, Watchers.getWatcherForType(player, entityType)));
    }

    public void unRenderEntity(Player player)
    {
        registry.unregister(player.getUniqueId());
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();
    }
}
