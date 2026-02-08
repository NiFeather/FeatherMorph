package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ZombiePropertyCollection;

public class ZombieWatcher extends LivingEntityWatcher
{
    public ZombieWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.ZOMBIE);
    }

    protected ZombieWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(ZombiePropertyCollection.class);

        if (property.equals(properties.IS_BABY))
            this.writePersistent(ValueIndex.ZOMBIE.IS_BABY, (Boolean) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ZOMBIE);
    }
}
