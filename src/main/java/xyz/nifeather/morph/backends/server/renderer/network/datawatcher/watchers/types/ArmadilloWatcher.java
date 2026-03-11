package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import org.bukkit.Sound;
import org.bukkit.entity.Armadillo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

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
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.ARMADILLO_STATE))
        {
            var state = (Armadillo.State) value;
            var peState = switch (state)
            {
                case IDLE -> ArmadilloState.IDLE;
                case ROLLING -> ArmadilloState.ROLLING;
                case SCARED -> ArmadilloState.SCARED;
                case UNROLLING -> ArmadilloState.UNROLLING;
            };

            this.writePersistent(ValueIndex.ARMADILLO.STATE, peState);
        }
    }
}
