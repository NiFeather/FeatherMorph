package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class GoatValues extends AnimalValues
{
    public final SingleValue<Boolean> IS_SCREAMING = createSingle("goat_is_screaming", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> HAS_LEFT_HORN = createSingle("goat_has_left_horn", true, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> HAS_RIGHT_HORN = createSingle("goat_has_right_horn", true, EntityDataTypes.BOOLEAN);

    public GoatValues()
    {
        super();

        registerSingle(IS_SCREAMING, HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }
}
