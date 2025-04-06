package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class AxolotlValues extends AnimalValues
{
    public final SingleValue<Integer> COLOR = createSingle("axolotl_color", 0, EntityDataTypes.INT);
    public final SingleValue<Boolean> PLAYING_DEAD = createSingle("axolotl_playing_dead", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> SPAWNED_FROM_BUCKET = createSingle("axolotl_spawned_from_bucket", false, EntityDataTypes.BOOLEAN);

    public AxolotlValues()
    {
        super();

        registerSingle(COLOR, PLAYING_DEAD, SPAWNED_FROM_BUCKET);
    }
}
