package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class ChestedHorseValues extends AbstractHorseValues
{
    public final SingleValue<Boolean> HAS_CHEST = createSingle("chested_horse_has_chest", false, EntityDataTypes.BOOLEAN);

    public ChestedHorseValues()
    {
        super();

        registerSingle(HAS_CHEST);
    }
}
