package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public abstract class AbstractNautilusWatcher extends TameableAnimalWatcher
{
    protected AbstractNautilusWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property.id().equals(PropertyNames.NAUTILUS_DASHING))
        {
            var dashing = (Boolean) value;
            this.writePersistent(ValueIndex.NAUTILUS_COMMON.DASHING, dashing);
        }

        super.onPropertyWrite(property, value);
    }
}
