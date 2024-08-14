package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class ZoglinWatcher extends EHasAttackAnimationWatcher
{
    public ZoglinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ZOGLIN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ZOGLIN);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("IsBaby"))
            writeOverride(ValueIndex.ZOGLIN.IS_BABY, nbt.getBoolean("IsBaby"));
    }
}
