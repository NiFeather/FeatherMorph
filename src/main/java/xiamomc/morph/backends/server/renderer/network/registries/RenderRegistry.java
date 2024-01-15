package xiamomc.morph.backends.server.renderer.network.registries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class RenderRegistry extends MorphPluginObject
{
    public record EventParameters(Player player, SingleWatcher parameters)
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

    private final Map<UUID, SingleWatcher> watcherMap = new Object2ObjectOpenHashMap<>();

    @Nullable
    public SingleWatcher getWatcher(Entity entity)
    {
        return getWatcher(entity.getUniqueId());
    }

    @Nullable
    public SingleWatcher getWatcher(UUID uuid)
    {
        boolean locked;

        try
        {
            locked = readLock.tryLock(rwLockWaitTime, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Unable to lock registry for read!", t);
        }

        if (!locked)
            throw new RuntimeException("Unable to lock registry for read: Timed out");

        SingleWatcher watcher;

        try
        {
            watcher = watcherMap.getOrDefault(uuid, null);
        }
        finally
        {
            readLock.unlock();
        }

        return watcher;
    }

    public void unregister(Player player)
    {
        unregister(player.getUniqueId());
    }

    public void unregister(UUID uuid)
    {
        var watcher = watcherMap.remove(uuid);
        callUnregister(Bukkit.getPlayer(uuid), watcher);

        watcher.dispose();
    }

    /**
     * 注册玩家的伪装类型
     * @param player 目标玩家
     * @param registerParameters 注册参数
     */
    public SingleWatcher register(@NotNull Player player, RegisterParameters registerParameters)
    {
        var watcher = WatcherIndex.getInstance().getWatcherForType(player, registerParameters.entityType());
        watcher.write(EntryIndex.DISGUISE_NAME, registerParameters.name());
        register(player.getUniqueId(), watcher);

        return watcher;
    }

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final int rwLockWaitTime = 20;

    /**
     * 注册UUID对应的伪装类型
     * @param uuid 目标玩家的UUID
     * @param watcher 对应的 {@link SingleWatcher}
     */
    public void register(@NotNull UUID uuid, @NotNull SingleWatcher watcher)
    {
        if (watcher == null)
            throw new NullDependencyException("Null Watcher!");

        boolean locked;
        try
        {
            locked = writeLock.tryLock(rwLockWaitTime, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Unable to lock registry for write!", t);
        }

        if (!locked)
            throw new RuntimeException("Unable to lock registry for write: Timed out.");

        try
        {
            watcherMap.put(uuid, watcher);
            callRegister(Bukkit.getPlayer(uuid), watcher);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void reset()
    {
        watcherMap.forEach((uuid, watcher) ->
        {
            callRegister(Bukkit.getPlayer(uuid), watcher);
        });

        watcherMap.clear();
    }

    //endregion Registry
}
