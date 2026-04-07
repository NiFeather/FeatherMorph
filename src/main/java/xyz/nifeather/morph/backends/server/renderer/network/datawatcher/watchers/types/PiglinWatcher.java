package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class PiglinWatcher extends LivingEntityWatcher
{
    public PiglinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PIGLIN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PIGLIN);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.PIGLIN_DANCING))
        {
            var dancing = (Boolean) value;
            this.writePersistent(ValueIndex.PIGLIN.DANCING, dancing);
        }
    }
}
