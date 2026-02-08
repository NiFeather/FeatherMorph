package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;

public class ZombieHorseWatcher extends AbstractHorseWatcher
{
    public ZombieHorseWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.ZOMBIE_HORSE);
    }
}
