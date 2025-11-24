package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PhantomPropertyCollection;

import java.util.Objects;

public class PhantomWatcher extends LivingEntityWatcher
{
    public PhantomWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PHANTOM);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PHANTOM);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var phantomProperties = DisguiseProperties.INSTANCE.getOrThrow(PhantomPropertyCollection.class);

        if (property.equals(phantomProperties.SIZE))
            this.writePersistent(ValueIndex.PHANTOM.SIZE, (Integer) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    public <X> @Nullable X readEntry(CustomEntry<X> entry)
    {
        if (Objects.equals(entry, CustomEntries.OVERLAYED_PITCH))
            return (X) Float.valueOf(-getBindingPlayer().getPitch());

        return super.readEntry(entry);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Size", read(ValueIndex.PHANTOM.SIZE));
    }
}
