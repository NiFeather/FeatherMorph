package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class AbstractHorseWatcher extends AgeableMobWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ABSTRACT_HORSE);
    }

    public AbstractHorseWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Age"))
            writeOverride(ValueIndex.ABSTRACT_HORSE.IS_BABY, nbt.getInt("Age") < 0);
    }
}
