package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;

public class MagmaWatcher extends AbstractSlimeWatcher
{
    public MagmaWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.MAGMA_CUBE);
    }
}
