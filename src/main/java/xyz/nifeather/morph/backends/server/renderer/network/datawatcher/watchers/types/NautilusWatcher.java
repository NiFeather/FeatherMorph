package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class NautilusWatcher extends AbstractNautilusWatcher
{
    public NautilusWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.NAUTILUS);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.NAUTILUS);
    }
}
