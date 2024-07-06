package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class SheepValues extends AnimalValues
{
    public final SingleValue<Byte> WOOL_TYPE = getSingle("sheep_wool_type", (byte)0, EntityDataTypes.BYTE);

    public SheepValues()
    {
        super();

        registerSingle(WOOL_TYPE);
    }
}
