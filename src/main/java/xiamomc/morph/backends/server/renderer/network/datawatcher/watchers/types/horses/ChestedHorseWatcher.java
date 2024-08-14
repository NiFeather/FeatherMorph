package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class ChestedHorseWatcher extends AbstractHorseWatcher
{
    public ChestedHorseWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CHESTED_HORSE);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("ChestedHorse"))
            writeOverride(ValueIndex.CHESTED_HORSE.HAS_CHEST, nbt.getBoolean("ChestedHorse"));
    }
}
