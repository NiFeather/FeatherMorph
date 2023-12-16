package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class MooshroomWatcher extends LivingEntityWatcher
{
    public MooshroomWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MUSHROOM_COW);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.MOOSHROOM);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Type"))
            write(ValueIndex.MOOSHROOM.VARIANT, nbt.getString("Type"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putString("Type", get(ValueIndex.MOOSHROOM.VARIANT));
    }
}
