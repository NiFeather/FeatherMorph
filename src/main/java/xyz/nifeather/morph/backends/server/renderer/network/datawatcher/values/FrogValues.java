package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariant;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariants;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class FrogValues extends AnimalValues
{
    public final SingleValue<FrogVariant> FROG_VARIANT = createSingle("frog_variant", FrogVariants.TEMPERATE, EntityDataTypes.TYPED_FROG_VARIANT);

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
