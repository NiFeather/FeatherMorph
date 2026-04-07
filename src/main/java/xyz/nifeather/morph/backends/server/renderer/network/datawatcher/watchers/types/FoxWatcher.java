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
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.FoxPropertyCollection;

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
        switch (property.id())
        {
            case PropertyNames.FOX_VARIANT ->
            {
                var val = (Fox.Type) value;

                this.writePersistent(ValueIndex.FOX.FOX_VARIANT, val.ordinal());
            }

            case PropertyNames.FOX_STATUS ->
            {
                var status = (FoxPropertyCollection.FoxStatus) value;
                var flag = switch (status)
                {
                    case STANDING -> 0x00;
                    case SITTING -> 0x01;
                    case SLEEPING -> 0x20;
                };

                this.writePersistent(ValueIndex.FOX.FLAGS, (byte)flag);
            }
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var foxType = this.read(ValueIndex.FOX.FOX_VARIANT) == 0 ? "red" : "snow";
        nbt.putString("Type", foxType);
    }
}
