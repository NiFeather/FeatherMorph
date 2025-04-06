package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.cow.CowVariant;
import com.github.retrooper.packetevents.protocol.entity.cow.CowVariants;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class CowValues extends AnimalValues
{
    public final SingleValue<CowVariant> COW_VARIANT;

    public CowValues()
    {
        COW_VARIANT = createSingle("cow_variant", CowVariants.TEMPERATE, EntityDataTypes.COW_VARIANT);

        registerSingle(COW_VARIANT);
    }
}
