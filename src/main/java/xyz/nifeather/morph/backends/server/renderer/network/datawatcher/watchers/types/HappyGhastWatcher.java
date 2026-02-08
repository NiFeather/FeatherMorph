package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.HappyGhastPropertyCollection;

public class HappyGhastWatcher extends LivingEntityWatcher
{
    private final HappyGhastPropertyCollection properties;

    public HappyGhastWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.HAPPY_GHAST);

        properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(HappyGhastPropertyCollection.class);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();
        register(ValueIndex.HAPPY_GHAST);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        nbt.putInt("Age", this.read(ValueIndex.HAPPY_GHAST.IS_BABY) ? Integer.MIN_VALUE : 0);

        super.writeToCompound(nbt);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property.equals(properties.IS_GHASTLING))
        {
            this.writePersistent(ValueIndex.HAPPY_GHAST.IS_BABY, (Boolean) value);
            return;
        }

        super.onPropertyWrite(property, value);
    }
}
