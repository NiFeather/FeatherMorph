package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class ChestedHorseWatcher extends AbstractHorseWatcher
{
    public ChestedHorseWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CHESTED_HORSE);
    }

}
