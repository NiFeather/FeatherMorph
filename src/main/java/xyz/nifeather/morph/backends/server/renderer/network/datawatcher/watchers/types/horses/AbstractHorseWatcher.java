package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class AbstractHorseWatcher extends AgeableMobWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ABSTRACT_HORSE);
    }

    public AbstractHorseWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }
}
