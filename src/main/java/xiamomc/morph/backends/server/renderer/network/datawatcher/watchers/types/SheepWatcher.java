package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class SheepWatcher extends LivingEntityWatcher
{
    public SheepWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SHEEP);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SHEEP);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Color"))
            writePersistent(ValueIndex.SHEEP.WOOL_TYPE, nbt.getByte("Color"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("Color", read(ValueIndex.SHEEP.WOOL_TYPE));
    }
}
