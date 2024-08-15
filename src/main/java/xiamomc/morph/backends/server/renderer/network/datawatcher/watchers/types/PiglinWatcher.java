package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
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
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.ANIMATION))
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
