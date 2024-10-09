package xyz.nifeather.morph.backends.server.renderer.network.registries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RenderRegistry extends MorphPluginObject
{
    public record EventParameters(Player player, SingleWatcher watcher)
    {
    }

    private final Map<Object, Consumer<EventParameters>> onRegisterConsumers = new Object2ObjectOpenHashMap<>();
    private final Map<Object, Consumer<EventParameters>> unRegisterConsumers = new Object2ObjectOpenHashMap<>();
    private final Map<Object, Consumer<EventParameters>> onRegistryChangeConsumers = new Object2ObjectOpenHashMap<>();

    public void onRegistryChange(Object source, Consumer<EventParameters> consumer)
    {
        onRegistryChangeConsumers.put(source, consumer);
    }

    public void onRegister(Object source, Consumer<EventParameters> consumer)
    {
        onRegisterConsumers.put(source, consumer);
    }

    private void callRegister(Player player, SingleWatcher watcher)
    {
        var ep = new EventParameters(player, watcher);
        onRegisterConsumers.forEach((source, consumer) -> consumer.accept(ep));
    }

    public void onUnRegister(Object source, Consumer<EventParameters> consumer)
    {
        unRegisterConsumers.put(source, consumer);
    }

    private void callUnregister(Player player, SingleWatcher watcher)
    {
        unRegisterConsumers.forEach((source, consumer) -> consumer.accept(new EventParameters(player, watcher)));
    }

    //region Registry

    private final Map<UUID, SingleWatcher> watcherMap = new ConcurrentHashMap<>();

    public List<SingleWatcher> getWatchers()
    {
        return new ObjectArrayList<>(watcherMap.values());
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public SingleWatcher getWatcher(Entity entity)
    {
        return getWatcher(entity.getUniqueId());
    }

    @Nullable
    public SingleWatcher getWatcher(UUID uuid)
    {
        return watcherMap.getOrDefault(uuid, null);
    }

    public void unregister(Player player)
    {
        unregister(player.getUniqueId());
    }

    /**
     * @param uuid The player's UUID
     * @return A watcher that binds to the UUID, if present
     * @apiNote The watcher returned is disposed
     */
    @Nullable
    public SingleWatcher unregister(UUID uuid)
    {
        var watcher = watcherMap.remove(uuid);
        if (watcher == null) return null;

        callUnregister(Bukkit.getPlayer(uuid), watcher);

        watcher.setParentRegistry(null);
        watcher.dispose();

        return watcher;
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    private final Bindable<String> randomBase = new Bindable<>("Stateof");

    public RenderRegistry()
    {
        config.bind(randomBase, ConfigOption.UUID_RANDOM_BASE);
    }

    /**
     * 注册玩家的伪装类型
     * @param player 目标玩家
     * @param registerParameters 注册参数
     * @param watcherConsumer Watcher的编辑函数，用于在注册事件前编辑Watcher的各项属性
     */
    public SingleWatcher register(@NotNull Player player, RegisterParameters registerParameters, Consumer<SingleWatcher> watcherConsumer)
    {
        var watcher = WatcherIndex.getInstance().getWatcherForType(player, registerParameters.entityType());

        watcher.markSilent(this);

        //设定初始值
        watcher.writeEntry(CustomEntries.DISGUISE_NAME, registerParameters.name());
        watcher.writeEntry(CustomEntries.SPAWN_ID, player.getEntityId());

        var str = randomBase.get()
                + registerParameters.entityType().toString()
                + registerParameters.name()
                + player.getName();

        var virtualEntityUUID = UUID.nameUUIDFromBytes(str.getBytes());
        watcher.writeEntry(CustomEntries.SPAWN_UUID, virtualEntityUUID);

        watcherConsumer.accept(watcher);

        registerWithWatcher(player.getUniqueId(), watcher);
        watcher.unmarkSilent(this);

        return watcher;
    }

    /**
     * 注册UUID对应的伪装类型
     * @param uuid 目标玩家的UUID
     * @param watcher 对应的 {@link SingleWatcher}
     */
    public void registerWithWatcher(@NotNull UUID uuid, @NotNull SingleWatcher watcher)
    {
        if (!watcher.getBindingPlayer().getUniqueId().equals(uuid))
            throw new IllegalArgumentException("Watcher UUID doesn't match with player's UUID!");

        watcherMap.put(uuid, watcher);

        watcher.setParentRegistry(this);
        callRegister(Bukkit.getPlayer(uuid), watcher);
    }

    public void reset()
    {
        watcherMap.forEach((uuid, watcher) ->
        {
            unregister(uuid);
            //callUnregister(Bukkit.getPlayer(uuid), watcher);
        });

        watcherMap.clear();
    }

    //endregion Registry
}
