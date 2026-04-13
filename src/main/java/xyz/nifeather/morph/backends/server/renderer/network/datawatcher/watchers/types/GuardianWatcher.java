package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.GuardianPropertyCollection;

public class GuardianWatcher extends LivingEntityWatcher
{
    public GuardianWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.GUARDIAN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.GUARDIAN);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(GuardianPropertyCollection.class);

        if (property.equals(properties.ATTACK_TARGET))
        {
            // Set to 0 to hide the attack beam
            var val = (Integer) value;
            this.writePersistent(ValueIndex.GUARDIAN.TARGET_ENTITY_ID, val);
        }

        super.onPropertyWrite(property, value);
    }
}
