package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ZombieHorseWatcher extends AbstractHorseWatcher
{
    public ZombieHorseWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ZOMBIE_HORSE);
    }
}
