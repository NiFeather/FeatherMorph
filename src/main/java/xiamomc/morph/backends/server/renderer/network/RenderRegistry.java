package xiamomc.morph.backends.server.renderer.network;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.PlayerRideable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.Watchers;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class RenderRegistry extends MorphPluginObject
{
    public record EventParameters(Player player, RegistryParameters parameters)
    {
    }

    private final Map<Object, Consumer<EventParameters>> onRegisterConsumers = new Object2ObjectOpenHashMap<>();
    private final Map<Object, Consumer<Player>> unRegisterConsumers = new Object2ObjectOpenHashMap<>();

    public void onRegister(Object source, Consumer<EventParameters> consumer)
    {
        onRegisterConsumers.put(source, consumer);
    }

    private void callRegister(Player player, RegistryParameters parameters)
    {
        var ep = new EventParameters(player, parameters);
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

    private final Map<UUID, RegistryParameters> registryParameters = new Object2ObjectOpenHashMap<>();

    @Nullable
    public RegistryParameters getParameters(UUID uuid)
    {
        return registryParameters.getOrDefault(uuid, null);
    }

    @Nullable
    public SingleWatcher getWatcher(Entity entity)
    {
        return getWatcher(entity.getUniqueId());
    }

    @Nullable
    public SingleWatcher getWatcher(UUID uuid)
    {
        var parameters = registryParameters.getOrDefault(uuid, null);

        return parameters == null ? null : parameters.singleWatcher();
    }

    public void unregister(Player player)
    {
        unregister(player.getUniqueId());
    }

    public void unregister(UUID uuid)
    {
        registryParameters.remove(uuid);
        callUnregister(Bukkit.getPlayer(uuid));
    }

    /**
     * 注册玩家的伪装类型
     * @param player 目标玩家
     * @param bukkitType 伪装的Bukkit生物类型，为null则移除注册
     */
    public void register(@NotNull Player player, @NotNull org.bukkit.entity.EntityType bukkitType)
    {
        var watcher = Watchers.getWatcherForType(player, bukkitType);
        register(player.getUniqueId(), bukkitType, watcher);
    }

    /**
     * 注册UUID对应的伪装类型
     * @param uuid 目标玩家的UUID
     * @param bukkitType 伪装的Bukkit生物类型，为null则移除注册
     */
    public void register(@NotNull UUID uuid, @NotNull org.bukkit.entity.EntityType bukkitType, SingleWatcher watcher)
    {
        register(uuid, RegistryParameters.fromBukkitType(bukkitType, watcher));
    }

    public void register(@NotNull UUID uuid, @NotNull RegistryParameters parameters)
    {
        if (parameters == null)
        {
            throw new NullDependencyException("Null RegistryParameters!");
        }
        else
        {
            registryParameters.put(uuid, parameters);
            callRegister(Bukkit.getPlayer(uuid), parameters);
        }
    }

    public void reset()
    {
        var players = registryParameters.keySet().stream().toList();
        registryParameters.clear();

        players.forEach(uuid -> callUnregister(Bukkit.getPlayer(uuid)));
    }

    //endregion Registry
}
