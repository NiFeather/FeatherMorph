package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class AxolotlValues extends AnimalValues
{
    public final SingleValue<Integer> COLOR = getSingle(0).withRandom(0, 1, 2, 3, 4);
    public final SingleValue<Boolean> PLAYING_DEAD = getSingle(false);
    public final SingleValue<Boolean> SPAWNED_FROM_BUCKET = getSingle(false);

    public AxolotlValues()
    {
        super();

        registerSingle(COLOR, PLAYING_DEAD, SPAWNED_FROM_BUCKET);
    }
}
