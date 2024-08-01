package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.FoxProperties;

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

            this.write(ValueIndex.FOX.FOX_VARIANT, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Type"))
        {
            var isSnow = nbt.getString("Type").equalsIgnoreCase("SNOW");
            write(ValueIndex.FOX.FOX_VARIANT, isSnow ? 1 : 0);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var foxType = this.get(ValueIndex.FOX.FOX_VARIANT) == 0 ? "red" : "snow";
        nbt.putString("Type", foxType);
    }
}
