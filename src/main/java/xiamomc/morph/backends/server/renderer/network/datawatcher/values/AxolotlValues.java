package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class AxolotlValues extends AnimalValues
{
    public final SingleValue<Integer> COLOR = getSingle("axolotl_color", 0, EntityDataTypes.INT).withRandom(0, 1, 2, 3, 4);
    public final SingleValue<Boolean> PLAYING_DEAD = getSingle("axolotl_playing_dead", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> SPAWNED_FROM_BUCKET = getSingle("axolotl_spawned_from_bucket", false, EntityDataTypes.BOOLEAN);

    public AxolotlValues()
    {
        super();

        registerSingle(COLOR, PLAYING_DEAD, SPAWNED_FROM_BUCKET);
    }
}
