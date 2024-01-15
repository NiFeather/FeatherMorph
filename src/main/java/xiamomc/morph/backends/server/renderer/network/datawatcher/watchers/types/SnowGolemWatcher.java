package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SnowGolemValues;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class SnowGolemWatcher extends LivingEntityWatcher
{
    public SnowGolemWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SNOWMAN);
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

            write(ValueIndex.SNOW_GOLEM.HAT_FLAGS, value);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var hatFlag = this.get(ValueIndex.SNOW_GOLEM.HAT_FLAGS);
        nbt.putBoolean("Pumpkin", hatFlag == SnowGolemValues.HAS_PUMPKIN);
    }
}
