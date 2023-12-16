package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class WolfWatcher extends TameableAnimalWatcher
{
    public WolfWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.WOLF);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.WOLF);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("CollarColor"))
            write(ValueIndex.WOLF.COLLAR_COLOR, (int)nbt.getByte("CollarColor"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("CollarColor", get(ValueIndex.WOLF.COLLAR_COLOR).byteValue());
    }
}
