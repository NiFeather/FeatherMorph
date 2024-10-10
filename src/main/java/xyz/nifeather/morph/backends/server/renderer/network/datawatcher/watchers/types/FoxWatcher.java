package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.FoxProperties;

public class FoxWatcher extends AgeableMobWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.FOX);
    }

    public FoxWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.FOX);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(FoxProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Fox.Type) value;

            this.writePersistent(ValueIndex.FOX.FOX_VARIANT, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.SLEEP -> this.writePersistent(ValueIndex.FOX.FLAGS, (byte)0x20);
                case AnimationNames.SIT -> this.writePersistent(ValueIndex.FOX.FLAGS, (byte)0x01);
                case AnimationNames.STANDUP, AnimationNames.RESET -> this.writePersistent(ValueIndex.FOX.FLAGS, (byte)0);
            }
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Type"))
        {
            var isSnow = nbt.getString("Type").equalsIgnoreCase("SNOW");
            writePersistent(ValueIndex.FOX.FOX_VARIANT, isSnow ? 1 : 0);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var foxType = this.read(ValueIndex.FOX.FOX_VARIANT) == 0 ? "red" : "snow";
        nbt.putString("Type", foxType);
    }
}
