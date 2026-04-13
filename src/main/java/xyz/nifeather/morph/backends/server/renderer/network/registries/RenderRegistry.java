package xyz.nifeather.morph.backends.server.renderer.network.registries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.BindableVirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RenderRegistry extends MorphPluginObject
{
    public record EventParameters(@Nullable LivingEntity entity, VirtualEntity watcher)
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

    private void callRegister(LivingEntity living, VirtualEntity watcher)
    {
        var ep = new EventParameters(living, watcher);
        onRegisterConsumers.forEach((source, consumer) -> consumer.accept(ep));
    }

    public void onUnRegister(Object source, Consumer<EventParameters> consumer)
    {
        unRegisterConsumers.put(source, consumer);
    }

    private void callUnregister(@Nullable LivingEntity living, VirtualEntity watcher)
    {
        unRegisterConsumers.forEach((source, consumer) -> consumer.accept(new EventParameters(living, watcher)));
    }

    //region Registry

    private final Map<UUID, VirtualEntity> watcherMap = new ConcurrentHashMap<>();

    public List<VirtualEntity> getWatchers()
    {
        return new ObjectArrayList<>(watcherMap.values());
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public VirtualEntity getWatcher(Entity entity)
    {
        return getWatcher(entity.getUniqueId());
    }

    @Nullable
    public VirtualEntity getWatcher(UUID uuid)
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
    public VirtualEntity unregister(UUID uuid)
    {
        var watcher = watcherMap.remove(uuid);
        if (watcher == null) return null;

        var entity = Bukkit.getEntity(uuid);
        callUnregister(entity instanceof LivingEntity living ? living : null, watcher);

        watcher.setParentRegistry(null);
        watcher.dispose();

        return watcher;
    }

    public void unregister(VirtualEntity virtualEntity)
    {
        var entry = watcherMap.entrySet().stream().filter(e -> e.getValue().equals(virtualEntity))
                .findFirst()
                .orElse(null);

        if (entry == null) return;

        watcherMap.remove(entry.getKey(), entry.getValue());
    }

    /**
     * 注册玩家的伪装类型
     * @param living 目标实体
     * @param registerParameters 注册参数
     * @param watcherConsumer Watcher的编辑函数，用于在注册事件前编辑Watcher的各项属性
     */
    public VirtualEntity register(@NotNull LivingEntity living, RegisterParameters registerParameters, Consumer<VirtualEntity> watcherConsumer)
        throws IllegalArgumentException
    {
        var watcher = WatcherIndex.getInstance().getWatcherForType(living, registerParameters.entityType());

        watcher.markSilent(this);

        //设定初始值
        watcher.writeEntry(CustomEntries.DISGUISE_NAME, registerParameters.name());
        watcher.writeEntry(CustomEntries.SPAWN_ID, living.getEntityId());

        watcherConsumer.accept(watcher);

        registerWithWatcher(living.getUniqueId(), watcher);
        watcher.unmarkSilent(this);

        return watcher;
    }

    /**
     * 注册UUID对应的伪装类型
     * @param uuid 目标玩家的UUID
     * @param watcher 对应的 {@link VirtualEntity}
     */
    public void registerWithWatcher(@NotNull UUID uuid, @NotNull VirtualEntity watcher) throws IllegalArgumentException
    {
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
