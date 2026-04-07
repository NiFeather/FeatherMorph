package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.AllayPropertyCollection;

public class AllayWatcher extends LivingEntityWatcher
{
    private AllayPropertyCollection properties;

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ALLAY);
        this.properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(AllayPropertyCollection.class);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.ALLAY_DANCING))
        {
            var dancing = (Boolean) value;
            this.writePersistent(ValueIndex.ALLAY.DANCING, dancing);
        }
    }

    public AllayWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ALLAY);
    }

    @Override
    protected void doSync()
    {
        super.doSync();
    }
}
