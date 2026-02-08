package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariants;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieNautilus;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class ZombieNautilusWatcher extends AbstractNautilusWatcher
{
    public ZombieNautilusWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.ZOMBIE_NAUTILUS);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ZOMBIE_NAUTILUS);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property.id().equals(PropertyNames.ZOMBIE_NAUTILUS_VARIANT))
        {
            var variant = (ZombieNautilus.Variant) value;

            var peVariant = ZombieNautilusVariants.getRegistry().getByNameOrThrow(new ResourceLocation(variant.key().asString()));
            this.writePersistent(ValueIndex.ZOMBIE_NAUTILUS.VARIANT, peVariant);
        }

        super.onPropertyWrite(property, value);
    }
}
