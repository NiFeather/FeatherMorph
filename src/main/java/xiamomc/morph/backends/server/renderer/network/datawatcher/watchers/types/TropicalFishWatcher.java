package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

import java.util.Arrays;

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
    protected void initValues()
    {
        super.initValues();
/*
        var patterns = TropicalFish.Pattern.values();
        Arrays.stream(patterns).findAny().get();
 */

        //todo
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            write(ValueIndex.TROPICAL.FISH_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", get(ValueIndex.TROPICAL.FISH_VARIANT));
    }
}
