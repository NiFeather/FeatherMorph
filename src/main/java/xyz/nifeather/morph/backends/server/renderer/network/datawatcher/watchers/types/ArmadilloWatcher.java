package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

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
            var player = getBindingPlayer();
            var world = player.getWorld();
            switch (animId)
            {
                case AnimationNames.PANIC_ROLLING ->
                {
                    writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.ROLLING);
                    world.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_ROLL, 1, 1);
                }
                case AnimationNames.PANIC_SCARED ->
                {
                    writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.SCARED);
                    world.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_LAND, 1, 1);
                }
                case AnimationNames.PANIC_UNROLLING ->
                {
                    writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.UNROLLING);
                    world.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_UNROLL_START, 1, 1);
                }
                case AnimationNames.PANIC_IDLE, AnimationNames.RESET ->
                {
                    writePersistent(ValueIndex.ARMADILLO.STATE, Armadillo.ArmadilloState.IDLE);

                    if (animId.equals(AnimationNames.PANIC_IDLE))
                        world.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_UNROLL_FINISH, 1, 1);
                }
            }
        }
    }
}
