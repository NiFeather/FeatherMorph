package xyz.nifeather.morph.backends.server.renderer;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.DisplayParameters;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolHandler;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegisterParameters;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.BuildFailedException;

import java.util.List;

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
    @Nullable
    public SingleWatcher registerEntity(Player player, EntityType entityType, String name)
    {
        try
        {
            return registry.register(player, new RegisterParameters(entityType, name), w ->
            {
                if (w instanceof LivingEntityWatcher livingEntityWatcher)
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

    public void refreshStateForPlayer(@Nullable Player player, List<Player> affectedPlayers)
            throws BuildFailedException
    {
        if (player == null) return;

        var watcher = registry.getWatcher(player.getUniqueId());
        if (watcher == null)
            throw new NullDependencyException("Null Watcher for a existing player?!");

        refreshStateForPlayer(player,
                new DisplayParameters(watcher),
                affectedPlayers);
    }

    /**
     * 刷新玩家的伪装
     * @param player 目标玩家
     * @param displayParameters 和伪装对应的 {@link DisplayParameters}
     */
    public void refreshStateForPlayer(@Nullable Player player, @NotNull DisplayParameters displayParameters, List<Player> affectedPlayers)
        throws BuildFailedException
    {
        if (affectedPlayers.isEmpty()) return;

        if (player == null) return;
        var watcher = displayParameters.getWatcher();

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
        var watcher = new PlayerWatcher(player);
        watcher.markSilent(this);

        watcher.writeEntry(CustomEntries.PROFILE, ((CraftPlayer) player).getProfile());
        watcher.writeEntry(CustomEntries.SPAWN_UUID, player.getUniqueId());
        watcher.writeEntry(CustomEntries.SPAWN_ID, player.getEntityId());
        watcher.writeEntry(CustomEntries.PROFILE_LISTED, true);
        watcher.writeEntry(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, true);

        List<PacketWrapper<?>> packets;

        try
        {
            packets = watcher.buildSpawnPackets();
        }
        catch (BuildFailedException e)
        {
            logger.error("PANIC! Can't undisguise player, BuildFailedException has been thrown!", e);
            return;
        }

        var removePacket = new WrapperPlayServerDestroyEntities(player.getEntityId());

        if (disguiseWatcher.getEntityType() == org.bukkit.entity.EntityType.PLAYER)
        {
            var disguiseUUID = disguiseWatcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);
            var packetRemoveInfo = new WrapperPlayServerPlayerInfoRemove(disguiseUUID);

            featherMorph().getPlatform()
                    .onlinePlayers()
                    .forEach(p -> protocolManager.sendPacket(p, packetRemoveInfo));
        }

        watcher.dispose();

        affectedPlayers.forEach(p ->
        {
            protocolManager.sendPacket(p, removePacket);

            for (var packet : packets)
                protocolManager.sendPacket(p, packet);
        });
    }

    public void dispose()
    {
        registry.reset();
        protocolHandler.dispose();

        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}
