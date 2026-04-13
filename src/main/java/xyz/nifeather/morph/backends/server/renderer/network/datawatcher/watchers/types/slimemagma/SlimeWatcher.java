package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;

public class SlimeWatcher extends AbstractSlimeWatcher
{
    public SlimeWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.SLIME);
    }
}
