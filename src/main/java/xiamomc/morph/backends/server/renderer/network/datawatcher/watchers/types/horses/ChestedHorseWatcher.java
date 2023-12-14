package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;

public class ChestedHorseWatcher extends AbstractHorseWatcher
{
    public ChestedHorseWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        var horseChested = nbt.getBoolean("ChestedHorse");
        write(ValueIndex.CHESTED_HORSE.HAS_CHEST, horseChested);
    }
}
