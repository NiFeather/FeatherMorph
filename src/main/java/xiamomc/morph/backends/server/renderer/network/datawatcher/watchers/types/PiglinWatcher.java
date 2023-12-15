package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class PiglinWatcher extends LivingEntityWatcher
{
    public PiglinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PIGLIN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PIGLIN);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("IsBaby"))
            write(ValueIndex.PIGLIN.IS_BABY, nbt.getBoolean("IsBaby"));

        //if (nbt.contains("IsImmuneToZombification"))
        //    write(ValueIndex.PIGLIN.IMMUNE_TO_ZOMBIFICATION, nbt.getBoolean("IsImmuneToZombification"));
    }
}
