package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class ArmadilloWatcher extends LivingEntityWatcher
{
    public ArmadilloWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ARMADILLO);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        this.register(ValueIndex.ARMADILLO);
    }

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();
            switch (animId)
            {
                case AnimationNames.PANIC_ROLLING -> writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.ROLLING);
                case AnimationNames.PANIC_SCARED -> writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.SCARED);
                case AnimationNames.PANIC_UNROLLING -> writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.UNROLLING);
                case AnimationNames.PANIC_IDLE, AnimationNames.RESET -> writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.IDLE);
            }
        }
    }
}
