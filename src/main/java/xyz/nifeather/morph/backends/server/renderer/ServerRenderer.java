package xyz.nifeather.morph.backends.server.renderer;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.ServerBackend;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolHandler;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegisterParameters;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.ExecutionErrorException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            var player = parameters.player();
            if (player == null)
                return;

            synchronized (cachedPlayerAffects)
            {
                cachedPlayerAffects.remove(parameters.watcher());
            }

            this.unDisguiseForPlayer(player, parameters.watcher(), WatcherUtils.getAffectedPlayers(player));
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
     * @param player 目标玩家
     * @param entityType 目标类型
     * @param name 伪装名称
     * @throws ExecutionErrorException If there's an error while registering the player
     */
    @NotNull
    public SingleWatcher registerEntity(Player player, EntityType entityType, String name) throws ExecutionErrorException
    {
        try
        {
            return registry.register(player, new RegisterParameters(entityType, name), w ->
            {
                if (w instanceof LivingEntityWatcher livingEntityWatcher)
                    livingEntityWatchers.add(livingEntityWatcher);
            });
        }
        catch (Exception e)
        {
            unRegisterEntity(player);

            throw ExecutionErrorException.forMethod("registerEntity")
                    .causedBy(e)
                    .withMessage("Can't register player")
                    .create();
        }
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
            logger.error("Can't unregister player", t);
        }
    }

    /**
     * Send the disguise to the given affected players
     *
     * @param watcher
     */
    public void sendDisguise(@NotNull SingleWatcher watcher, List<Player> affectedPlayers)
        throws BuildFailedException
    {
        if (affectedPlayers.isEmpty()) return;

        var protocolManager = PacketEvents.getAPI().getPlayerManager();
        var spawnPackets = watcher.buildSpawnPackets();

        affectedPlayers.forEach(p ->
        {
            spawnPackets.forEach(packet -> protocolManager.sendPacket(p, packet));
        });
    }

    public void unDisguiseForPlayer(@Nullable Player player,
                                    SingleWatcher disguiseWatcher,
                                    List<Player> affectedPlayers)
    {
        if (player == null) return;

        var protocolManager = PacketEvents.getAPI().getPlayerManager();
        PlayerWatcher watcher = new PlayerWatcher(player);
        watcher.markSilent(this);

        watcher.writeEntry(CustomEntries.PROFILE, ((CraftPlayer) player).getProfile());
        watcher.writeEntry(CustomEntries.SPAWN_UUID, player.getUniqueId());
        watcher.writeEntry(CustomEntries.SPAWN_ID, player.getEntityId());
        watcher.writeEntry(CustomEntries.PROFILE_LISTED, true);
        watcher.writeEntry(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, true);

        List<PacketWrapper<?>> playerSpawnPackets;

        try
        {
            playerSpawnPackets = watcher.buildSpawnPackets(false);
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

        for (Player p : affectedPlayers)
        {
            for (PacketWrapper<?> removePacket : disposalPackets)
                protocolManager.sendPacket(p, removePacket);

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

    //region Scheduled disguising

    private final Map<SingleWatcher, List<Player>> cachedPlayerAffects = new ConcurrentHashMap<>();

    /**
     * Schedule displaying disguise for the given player
     * @return {@code true} if schedule success
     */
    public boolean scheduleDisguise(SingleWatcher watcher, List<Player> affectedPlayers)
    {
        var player = watcher.getBindingPlayer();
        if (!player.isOnline()) return false;

        // We cache affected players for the given watcher so that
        synchronized (cachedPlayerAffects)
        {
            var list = cachedPlayerAffects.getOrDefault(watcher, null);
            if (list != null) // List is not null: We already have task for refresh the disguise!
            {
                list.addAll(affectedPlayers);

                return true;
            }

            list = ObjectLists.synchronize(new ObjectArrayList<>(affectedPlayers));
            cachedPlayerAffects.put(watcher, list);
        }

        player.getScheduler().run(plugin, task ->
        {
            if (watcher.disposed() || task.isCancelled())
                return;

            List<Player> targetPlayers;
            synchronized (cachedPlayerAffects)
            {
                var affect = cachedPlayerAffects.remove(watcher);
                if (affect == null || affect.isEmpty())
                    return;

                targetPlayers = affect;
            }

            try
            {
                sendDisguise(watcher, targetPlayers);
            }
            catch (Exception e)
            {
                //todo: Make exception handling more gracefully
                //      For example, let DisguiseState or MorphManager subscribe for exceptions thrown from the renderer, not letting renderer call DisguiseState#handleException
                Objects.requireNonNull(ServerBackend.getInstance()).onDisguiseException(watcher, e);
            }
        }, () ->
        {
            // Should we do something if the entity is removed?
        });

        return true;
    }

    //endregion Scheduled disguising
}
