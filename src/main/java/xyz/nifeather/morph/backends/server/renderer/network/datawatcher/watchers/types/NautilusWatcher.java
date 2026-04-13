package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class NautilusWatcher extends AbstractNautilusWatcher
{
    public NautilusWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.NAUTILUS);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.NAUTILUS);
    }
}
