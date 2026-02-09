package xyz.nifeather.morph.backends.server.renderer;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolHandler;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegisterParameters;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.WatcherIndex;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PropertyCollection;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ServerRenderer extends MorphPluginObject implements Listener
{
    private final ProtocolHandler protocolHandler;

    public final RenderRegistry registry = new RenderRegistry();

    public ServerRenderer()
    {
        dependencies.cache(registry);
        dependencies.cache(protocolHandler = new ProtocolHandler());

        registry.onUnRegister(this, parameters ->
        {
            var entity = parameters.entity();
            if (entity == null)
                return;

            this.unDisguiseForEntity(entity, parameters.watcher());
        });
    }

    @Initializer
    private void load(MorphConfigManager config)
    {
        // 当前插件中有在禁用过程使用LivingEntityWatcher的处理
        // 因此在这里加上插件是否启用的检查
        if (plugin.isEnabled())
            Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private final List<LivingEntityWatcher> livingEntityWatchers = Collections.synchronizedList(new ObjectArrayList<>());

    @EventHandler
    public void onPlayerStartUsingItem(PlayerInteractEvent event)
    {
        for (var watcher : livingEntityWatchers)
            watcher.onPlayerStartUsingItem(event);
    }

    /**
     * 向后端渲染器注册玩家
     * @param entityType 目标类型
     * @param name 伪装名称
     * @throws ExecutionErrorException If there's an error while registering the player
     */
    @NotNull
    public VirtualEntity registerEntity(LivingEntity entity, EntityType entityType, String name) throws ExecutionErrorException
    {
        try
        {
            return registry.register(entity, new RegisterParameters(entityType, name), w ->
            {
                if (w instanceof LivingEntityWatcher livingEntityWatcher)
                    livingEntityWatchers.add(livingEntityWatcher);
            });
        }
        catch (Exception e)
        {
            unRegisterEntity(entity);

            throw ExecutionErrorException.forMethod("registerEntity")
                    .causedBy(e)
                    .withMessage("Can't register player")
                    .create();
        }
    }

    public void unRegisterEntity(LivingEntity entity)
    {
        try
        {
            var watcher = registry.unregister(entity.getUniqueId());

            if (watcher != null)
                this.livingEntityWatchers.remove(watcher);
        }
        catch (Throwable t)
        {
            logger.error("Can't unregister player", t);
        }
    }

    public void spawnVirtualEntity(@NotNull UUID entityUUID)
            throws BuildFailedException, NullDependencyException
    {
        var watcher = registry.getWatcher(entityUUID);
        if (watcher == null)
            throw new NullDependencyException("Null Watcher for a existing player?!");

        spawnVirtualEntity(watcher, watcher.getAffectedPlayers());
    }

    /**
     * 刷新玩家的伪装
     */
    public void spawnVirtualEntity(@NotNull VirtualEntity virtualEntity, List<Player> affectedPlayers)
            throws BuildFailedException
    {
        if (affectedPlayers.isEmpty()) return;

        var protocolManager = PacketEvents.getAPI().getPlayerManager();
        var spawnPackets = virtualEntity.buildSpawnPackets();

        affectedPlayers.forEach(p ->
        {
            spawnPackets.forEach(packet -> protocolManager.sendPacket(p, packet));
        });
    }

    public void unDisguiseForEntity(@Nullable LivingEntity entity, VirtualEntity disguiseWatcher)
    {
        if (entity == null) return;

        var protocolManager = PacketEvents.getAPI().getPlayerManager();

        var watcher = WatcherIndex.getInstance().getWatcherForType(entity, entity.getType());
        watcher.markSilent(this);

        // Read properties from the entity
        var propertyHandler = new PropertyHandler();
        var properties = (PropertyCollection<LivingEntity>) DisguiseProperties.INSTANCE.getCollection(entity.getType());
        propertyHandler.registerFromPropertyCollection(properties);
        properties.setupPropertiesFromEntity(propertyHandler, entity);
        propertyHandler.getAll().forEach((p, v) -> watcher.writeProperty((SingleProperty<Object>) p, v));

        // Getting entity's ID need to be on their thread on Folia :D
        int entityID;
        try
        {
            entityID = FoliaThreadUtils.runOnEntitySync(entity, e ->
            {
                assert e != null; // 你哪里 nullable 了
                return e.getEntityId();
            }, FoliaThreadUtils.DEFAULT_WAIT_TIMEOUT);
        }
        catch (ExecutionException | TimeoutException | InterruptedException e)
        {
            logger.error("Can't get entity ID, aborting...", e);
            return;
        }

        watcher.writeEntry(CustomEntries.SPAWN_ID, entityID);
        watcher.writeEntry(CustomEntries.SPAWN_UUID, entity.getUniqueId());
        watcher.writeEntry(CustomEntries.PROFILE_LISTED, true);
        watcher.writeEntry(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, true);

        List<PacketWrapper<?>> playerSpawnPackets;

        try
        {
            playerSpawnPackets = watcher.buildSpawnPackets();
        }
        catch (BuildFailedException e)
        {
            logger.error("Can't build recover packets for player, BuildFailedException has been thrown!", e);
            return;
        }

        List<PacketWrapper<?>> disposalPackets = Collections.emptyList();
        try
        {
            disposalPackets = disguiseWatcher.buildVirtualEntityDisposalPackets();
        }
        catch (BuildFailedException e)
        {
            logger.error("Can't dispose virtual entity gracefully, BuildFailedException has been thrown!", e);
        }

        watcher.dispose();

        for (Player p : disguiseWatcher.getAffectedPlayers())
        {
            for (PacketWrapper<?> disposalPacket : disposalPackets)
                protocolManager.sendPacket(p, disposalPacket);

            for (var packet : playerSpawnPackets)
                protocolManager.sendPacket(p, packet);
        }
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();

        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}
