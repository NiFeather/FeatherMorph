package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class TropicalFishWatcher extends LivingEntityWatcher
{
    public TropicalFishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.TROPICAL_FISH);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TROPICAL);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            writePersistent(ValueIndex.TROPICAL.FISH_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", read(ValueIndex.TROPICAL.FISH_VARIANT));
    }
}
