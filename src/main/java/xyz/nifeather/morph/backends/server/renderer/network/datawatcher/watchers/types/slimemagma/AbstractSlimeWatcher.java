package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.SlimeMagmaPropertyCollection;

public class AbstractSlimeWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SLIME_MAGMA);
    }

    public AbstractSlimeWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(SlimeMagmaPropertyCollection.class);

        if (property.equals(properties.SIZE))
        {
            var size = (int) value;
            this.writeEntry(CustomEntries.SLIME_SIZE_REAL, size);
            this.writePersistent(ValueIndex.SLIME_MAGMA.SIZE, size);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Size", readEntryOrDefault(CustomEntries.SLIME_SIZE_REAL, 1));
    }
}
