package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class GoatValues extends AnimalValues
{
    public final SingleValue<Boolean> IS_SCREAMING = getSingle(false);
    public final SingleValue<Boolean> HAS_LEFT_HORN = getSingle(true);
    public final SingleValue<Boolean> HAS_RIGHT_HORN = getSingle(true);

    public GoatValues()
    {
        super();

        registerSingle(IS_SCREAMING, HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }
}
