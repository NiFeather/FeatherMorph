package xiamomc.morph.backends.server.renderer.network.registries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.WatcherIndex;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RenderRegistry extends MorphPluginObject
{
    public record EventParameters(Player player, SingleWatcher parameters)
    {
    }

    private final Map<Object, Consumer<EventParameters>> onRegisterConsumers = new Object2ObjectOpenHashMap<>();
    private final Map<Object, Consumer<Player>> unRegisterConsumers = new Object2ObjectOpenHashMap<>();
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

    public void onUnRegister(Object source, Consumer<Player> consumer)
    {
        unRegisterConsumers.put(source, consumer);
    }

    private void callUnregister(Player player)
    {
        unRegisterConsumers.forEach((source, consumer) -> consumer.accept(player));
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
        return watcherMap.getOrDefault(uuid, null);
    }

    public void unregister(Player player)
    {
        unregister(player.getUniqueId());
    }

    public void unregister(UUID uuid)
    {
        watcherMap.remove(uuid);
        callUnregister(Bukkit.getPlayer(uuid));
    }

    /**
     * 注册玩家的伪装类型
     * @param player 目标玩家
     * @param bukkitType 伪装的Bukkit生物类型，为null则移除注册
     */
    public SingleWatcher register(@NotNull Player player, RegisterParameters registerParameters)
    {
        var watcher = WatcherIndex.getInstance().getWatcherForType(player, registerParameters.entityType());
        watcher.write(EntryIndex.DISGUISE_NAME, registerParameters.name());
        register(player.getUniqueId(), watcher);

        return watcher;
    }

    /**
     * 注册UUID对应的伪装类型
     * @param uuid 目标玩家的UUID
     * @param watcher 对应的 {@link SingleWatcher}
     */
    public void register(@NotNull UUID uuid, @NotNull SingleWatcher watcher)
    {
        if (watcher == null)
        {
            throw new NullDependencyException("Null Watcher!");
        }
        else
        {
            watcherMap.put(uuid, watcher);
            callRegister(Bukkit.getPlayer(uuid), watcher);
        }
    }

    public void reset()
    {
        var players = watcherMap.keySet().stream().toList();
        watcherMap.clear();

        players.forEach(uuid -> callUnregister(Bukkit.getPlayer(uuid)));
    }

    //endregion Registry
}
