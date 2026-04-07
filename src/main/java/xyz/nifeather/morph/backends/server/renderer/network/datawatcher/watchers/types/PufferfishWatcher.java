package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.PufferfishValues;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PufferfishPropertyCollection;

public class PufferfishWatcher extends LivingEntityWatcher
{
    public PufferfishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PUFFERFISH);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        this.register(ValueIndex.PUFFERFISH);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.PUFFERFISH_PUFF_STATE))
        {
            var state = (PufferfishPropertyCollection.PufferfishState) value;

            int peState = switch (state)
            {
                case SMALL -> PufferfishValues.PuffStates.SMALL;
                case MID -> PufferfishValues.PuffStates.MID;
                case LARGE -> PufferfishValues.PuffStates.LARGE;
            };

            this.writePersistent(ValueIndex.PUFFERFISH.PUFF_STATE, peState);
        }
    }
}
