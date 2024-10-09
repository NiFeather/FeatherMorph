package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ParrotProperties;

public class ParrotWatcher extends TameableAnimalWatcher
{
    public ParrotWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PARROT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PARROT);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(ParrotProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Parrot.Variant) value;
            writePersistent(ValueIndex.PARROT.PARROT_VARIANT, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
        {
            var variant = nbt.getInt("Variant");
            this.writePersistent(ValueIndex.PARROT.PARROT_VARIANT, variant);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", read(ValueIndex.PARROT.PARROT_VARIANT));
    }
}
