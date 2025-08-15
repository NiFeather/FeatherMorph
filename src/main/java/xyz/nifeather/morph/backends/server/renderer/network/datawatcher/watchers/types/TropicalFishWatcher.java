package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.TropicalFishProperties;

public class TropicalFishWatcher extends LivingEntityWatcher
{
    public TropicalFishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.TROPICAL_FISH);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TROPICAL);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var tropicalProperties = DisguiseProperties.INSTANCE.getOrThrow(TropicalFishProperties.class);

        if (property.equals(tropicalProperties.VARIANT))
        {
            int val = (Integer) value;
            this.writePersistent(ValueIndex.TROPICAL.FISH_VARIANT, val);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", read(ValueIndex.TROPICAL.FISH_VARIANT));
    }
}
