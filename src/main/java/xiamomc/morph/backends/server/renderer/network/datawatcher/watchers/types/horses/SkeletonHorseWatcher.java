package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SkeletonHorseWatcher extends AbstractHorseWatcher
{
    public SkeletonHorseWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SKELETON_HORSE);
    }
}
