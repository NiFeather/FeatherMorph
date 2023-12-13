package xiamomc.morph.backends.server.renderer.network.datawatcher;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.ArmorStandValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.AllayWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.ArmorStandWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;

import java.util.Map;
import java.util.function.Function;

public class WatcherIndex
{
    public static WatcherIndex getInstance()
    {
        if (instance == null) instance = new WatcherIndex();
        return instance;
    }

    private static WatcherIndex instance;

    public WatcherIndex()
    {
        instance = this;

        setTypeWatcher(EntityType.PLAYER, PlayerWatcher::new);
        setTypeWatcher(EntityType.ALLAY, AllayWatcher::new);
        setTypeWatcher(EntityType.ARMOR_STAND, ArmorStandWatcher::new);
    }

    private void setTypeWatcher(EntityType type, Function<Player, SingleWatcher> func)
    {
        typeWatcherMap.put(type, func);
    }

    private final Map<EntityType, Function<Player, SingleWatcher>> typeWatcherMap = new Object2ObjectOpenHashMap<>();

    public SingleWatcher getWatcherForType(Player bindingPlayer, EntityType entityType)
    {
        var watcherFunc = typeWatcherMap.getOrDefault(entityType, null);

        if (watcherFunc == null)
            return new LivingEntityWatcher(bindingPlayer, entityType);

        return watcherFunc.apply(bindingPlayer);
    }
}
