package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.MooshroomValues;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.MooshroomPropertyCollection;

public class MooshroomWatcher extends LivingEntityWatcher
{
    public MooshroomWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MOOSHROOM);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.MOOSHROOM);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(MooshroomPropertyCollection.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (MushroomCow.Variant) value;
            writePersistent(ValueIndex.MOOSHROOM.DATA_TYPE, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putString("Type", read(ValueIndex.MOOSHROOM.DATA_TYPE) == MooshroomValues.RED ? "red" : "brown");
    }
}
