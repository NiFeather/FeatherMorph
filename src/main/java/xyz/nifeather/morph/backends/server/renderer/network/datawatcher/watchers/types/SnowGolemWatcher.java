package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SnowGolemValues;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

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
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Pumpkin"))
        {
            var value = nbt.getBoolean("Pumpkin")
                    ? SnowGolemValues.HAS_PUMPKIN
                    : SnowGolemValues.NO_PUMPKIN;

            writePersistent(ValueIndex.SNOW_GOLEM.HAT_FLAGS, value);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var hatFlag = this.read(ValueIndex.SNOW_GOLEM.HAT_FLAGS);
        nbt.putBoolean("Pumpkin", hatFlag == SnowGolemValues.HAS_PUMPKIN);
    }
}
