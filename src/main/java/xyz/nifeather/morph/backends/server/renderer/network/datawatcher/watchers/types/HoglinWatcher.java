package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.HoglinPropertyCollection;

public class HoglinWatcher extends EHasAttackAnimationWatcher
{
    public HoglinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.HOGLIN);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(HoglinPropertyCollection.class);

        if (property.equals(properties.IS_BABY))
            this.writePersistent(ValueIndex.AGEABLE_MOB.IS_BABY, (Boolean) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.AGEABLE_MOB);
    }
}
