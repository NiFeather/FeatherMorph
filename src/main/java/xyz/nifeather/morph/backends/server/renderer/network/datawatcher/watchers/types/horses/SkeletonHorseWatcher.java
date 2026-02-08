package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;

public class SkeletonHorseWatcher extends AbstractHorseWatcher
{
    public SkeletonHorseWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.SKELETON_HORSE);
    }
}
