package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;

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

        var size = Math.max(1, nbt.getInt("Size"));
        write(ValueIndex.SLIME_MAGMA.SIZE, size);
    }
}
