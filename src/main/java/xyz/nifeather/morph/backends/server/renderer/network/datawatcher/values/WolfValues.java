package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariants;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = createSingle("wolf_begging", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> COLLAR_COLOR = createSingle("wolf_collar_color", 14, EntityDataTypes.INT);
    public final SingleValue<Integer> ANGER_TIME = createSingle("wolf_anger_time", 0, EntityDataTypes.INT);
    public final SingleValue<Integer> WOLF_VARIANT = createSingle("wolf_variant", WolfVariants.PALE.getId(ClientVersion.getLatest()), EntityDataTypes.WOLF_VARIANT);

    public WolfValues()
    {
        super();

        registerSingle(WOLF_VARIANT, BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
