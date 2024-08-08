package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

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
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.DANCE_START -> this.write(ValueIndex.PIGLIN.DANCING, true);
                case AnimationNames.DANCE_STOP -> this.write(ValueIndex.PIGLIN.DANCING, false);
            }
        }
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
