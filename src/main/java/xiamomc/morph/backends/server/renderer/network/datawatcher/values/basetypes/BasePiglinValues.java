package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.MonsterValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class BasePiglinValues extends MonsterValues
{
    public final SingleValue<Boolean> IMMUNE_TO_ZOMBIFICATION = getSingle("piglin_immune_to_zombification", true, EntityDataTypes.BOOLEAN);

    public BasePiglinValues()
    {
        super();

        registerSingle(IMMUNE_TO_ZOMBIFICATION);
    }
}
