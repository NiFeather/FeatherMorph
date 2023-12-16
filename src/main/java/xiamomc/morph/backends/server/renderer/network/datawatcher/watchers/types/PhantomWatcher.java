package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class PhantomWatcher extends LivingEntityWatcher
{
    public PhantomWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PHANTOM);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PHANTOM);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Size"))
            write(ValueIndex.PHANTOM.SIZE, nbt.getInt("Size"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Size", get(ValueIndex.PHANTOM.SIZE));
    }
}
