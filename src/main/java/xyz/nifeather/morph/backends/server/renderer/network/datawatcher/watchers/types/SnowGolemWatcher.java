package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SnowGolemValues;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.SnowGolemPropertyCollection;

public class SnowGolemWatcher extends LivingEntityWatcher
{
    public SnowGolemWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SNOW_GOLEM);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SNOW_GOLEM);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var snowmanProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(SnowGolemPropertyCollection.class);

        if (property.equals(snowmanProperties.HAS_PUMPKIN))
        {
            var val = (Boolean) value;
            this.writePersistent(ValueIndex.SNOW_GOLEM.HAT_FLAGS, val ? SnowGolemValues.HAS_PUMPKIN : SnowGolemValues.NO_PUMPKIN);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var hatFlag = this.read(ValueIndex.SNOW_GOLEM.HAT_FLAGS);
        nbt.putBoolean("Pumpkin", hatFlag == SnowGolemValues.HAS_PUMPKIN);
    }
}
