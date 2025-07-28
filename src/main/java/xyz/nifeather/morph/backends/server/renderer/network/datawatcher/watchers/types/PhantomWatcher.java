package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

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
    public <X> @Nullable X readEntry(CustomEntry<X> entry)
    {
        if (Objects.equals(entry, CustomEntries.OVERLAYED_PITCH))
            return (X) Float.valueOf(-getBindingPlayer().getPitch());

        return super.readEntry(entry);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Size"))
            writePersistent(ValueIndex.PHANTOM.SIZE, nbt.getInt("Size"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Size", read(ValueIndex.PHANTOM.SIZE));
    }
}
