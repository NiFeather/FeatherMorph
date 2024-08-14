package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
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
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();
            switch (animId)
            {
                case AnimationNames.PANIC_ROLLING -> writeOverride(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.ROLLING);
                case AnimationNames.PANIC_SCARED -> writeOverride(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.SCARED);
                case AnimationNames.PANIC_UNROLLING -> writeOverride(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.UNROLLING);
                case AnimationNames.PANIC_IDLE, AnimationNames.RESET -> writeOverride(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.IDLE);
            }
        }
    }
}
