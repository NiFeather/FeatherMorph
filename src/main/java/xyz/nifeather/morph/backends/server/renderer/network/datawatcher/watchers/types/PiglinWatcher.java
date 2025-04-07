package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

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
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();
            var player = getBindingPlayer();
            var world = player.getWorld();

            switch (animId)
            {
                case AnimationNames.DANCE_START ->
                {
                    this.writePersistent(ValueIndex.PIGLIN.DANCING, true);
                    world.playSound(player.getLocation(), Sound.ENTITY_PIGLIN_CELEBRATE, 1, 1);
                }
                case AnimationNames.STOP, AnimationNames.RESET -> this.writePersistent(ValueIndex.PIGLIN.DANCING, false);
            }
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("IsBaby"))
            writePersistent(ValueIndex.PIGLIN.IS_BABY, nbt.getBoolean("IsBaby"));

        //if (nbt.contains("IsImmuneToZombification"))
        //    write(ValueIndex.PIGLIN.IMMUNE_TO_ZOMBIFICATION, nbt.getBoolean("IsImmuneToZombification"));
    }
}
