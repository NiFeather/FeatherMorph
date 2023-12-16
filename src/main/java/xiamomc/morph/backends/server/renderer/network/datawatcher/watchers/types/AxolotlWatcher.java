package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class AxolotlWatcher extends LivingEntityWatcher
{
    public AxolotlWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.AXOLOTL);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.AXOLOTL);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            write(ValueIndex.AXOLOTL.COLOR, nbt.getInt("Variant"));

        if (nbt.contains("FromBucket"))
            write(ValueIndex.AXOLOTL.SPAWNED_FROM_BUCKET, nbt.getBoolean("FromBucket"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", this.get(ValueIndex.AXOLOTL.COLOR));
        nbt.putBoolean("FromBucket", this.get(ValueIndex.AXOLOTL.SPAWNED_FROM_BUCKET));
    }
}
