package xiamomc.morph.backends.server.renderer;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.ProtocolHandler;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.RegisterParameters;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.UUID;

public class ServerRenderer extends MorphPluginObject implements Listener
{
    private final ProtocolHandler protocolHandler;

    public final RenderRegistry registry = new RenderRegistry();

    private final PacketFactory packetFactory = new PacketFactory();

    private final Bindable<Boolean> showPlayerDisguises = new Bindable<>();

    public ServerRenderer()
    {
        dependencies.cache(packetFactory);

        dependencies.cache(registry);
        dependencies.cache(protocolHandler = new ProtocolHandler());
    }

    @Initializer
    private void load(MorphConfigManager config)
    {
        // 当前插件中有在禁用过程使用LivingEntityWatcher的处理
        // 因此在这里加上插件是否启用的检查
        if (plugin.isEnabled())
            Bukkit.getPluginManager().registerEvents(this, plugin);

        config.bind(this.showPlayerDisguises, ConfigOption.SR_SHOW_PLAYER_DISGUISES_IN_TAB);
    }

    private final List<LivingEntityWatcher> livingEntityWatchers = new ObjectArrayList<>();

    @EventHandler
    public void onPlayerStartUsingItem(PlayerInteractEvent event)
    {
        for (var watcher : livingEntityWatchers)
            watcher.onPlayerStartUsingItem(event);
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
            return registry.register(player, new RegisterParameters(entityType, name), watcher ->
            {
                if (this.showPlayerDisguises.get())
                    watcher.writeEntry(CustomEntries.PROFILE_LISTED, true);

                if (watcher instanceof LivingEntityWatcher livingEntityWatcher)
                    livingEntityWatchers.add(livingEntityWatcher);
            });
        }
        catch (Throwable t)
        {
            logger.error("Can't register player: " + t.getMessage());
            t.printStackTrace();

            unRegisterEntity(player);
        }

        return null;
    }

    public void removePlayerFromTabList(UUID uuid)
    {
        var watcher = this.registry.getWatcher(uuid);

        if (watcher == null || watcher.getEntityType() != EntityType.PLAYER) return;

        var lastUUID = watcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);

        var packetRemoveInfo = PacketContainer.fromPacket(
                new ClientboundPlayerInfoRemovePacket(List.of(lastUUID)));

        var protocolManager = ProtocolLibrary.getProtocolManager();
        Bukkit.getOnlinePlayers().forEach(p -> protocolManager.sendServerPacket(p, packetRemoveInfo));
    }

    public void unRegisterEntity(Player player)
    {
        try
        {
            var watcher = registry.unregister(player.getUniqueId());

            if (watcher != null)
                this.livingEntityWatchers.remove(watcher);
        }
        catch (Throwable t)
        {
            logger.error("Can't unregister player: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();

        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}
