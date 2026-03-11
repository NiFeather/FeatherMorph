package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class CreakingWatcher extends EHasAttackAnimationWatcher
{
    public CreakingWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CREAKING);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CREAKING);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.CREAKING_EYES_GLOWING))
        {
            var glowing = (Boolean) value;
            this.writePersistent(ValueIndex.CREAKING.IS_ACTIVE, glowing);
        }
    }
}
