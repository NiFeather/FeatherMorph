package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class AxolotlValues extends AnimalValues
{
    public final SingleValue<Integer> COLOR = getSingle("axolotl_color", 0).withRandom(0, 1, 2, 3, 4);
    public final SingleValue<Boolean> PLAYING_DEAD = getSingle("axolotl_playing_dead", false);
    public final SingleValue<Boolean> SPAWNED_FROM_BUCKET = getSingle("axolotl_spawned_from_bucket", false);

    public AxolotlValues()
    {
        super();

        registerSingle(COLOR, PLAYING_DEAD, SPAWNED_FROM_BUCKET);
    }
}
