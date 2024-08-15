package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class AbstractSlimeWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SLIME_MAGMA);
    }

    public AbstractSlimeWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Size"))
        {
            // 史莱姆在通信时的大小比NBT中的大 1
            // 即：最小的史莱姆在NBT中的大小为0，在通信时传输的大小是1
            var size = Math.max(0, nbt.getInt("Size"));
            writePersistent(ValueIndex.SLIME_MAGMA.SIZE, size + 1);
            writeEntry(CustomEntries.SLIME_SIZE_REAL, size);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Size", readEntryOrDefault(CustomEntries.SLIME_SIZE_REAL, 1));
    }
}
