package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.AxolotlProperties;

public class AxolotlWatcher extends LivingEntityWatcher
{
    public AxolotlWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.AXOLOTL);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.AXOLOTL);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(AxolotlProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Axolotl.Variant) value;
            writePersistent(ValueIndex.AXOLOTL.COLOR, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            writePersistent(ValueIndex.AXOLOTL.COLOR, nbt.getInt("Variant"));

        if (nbt.contains("FromBucket"))
            writePersistent(ValueIndex.AXOLOTL.SPAWNED_FROM_BUCKET, nbt.getBoolean("FromBucket"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", this.read(ValueIndex.AXOLOTL.COLOR));
        nbt.putBoolean("FromBucket", this.read(ValueIndex.AXOLOTL.SPAWNED_FROM_BUCKET));
    }
}
