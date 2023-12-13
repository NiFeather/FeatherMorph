package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;

public class Watchers
{
    public static SingleWatcher getWatcherForType(Player bindingPlayer, EntityType entityType)
    {
        if (entityType == EntityType.PLAYER)
            return new PlayerWatcher(bindingPlayer, entityType);
        else
            return new LivingEntityWatcher(bindingPlayer, entityType);
    }
}
