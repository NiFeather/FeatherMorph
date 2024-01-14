package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class GoatValues extends AnimalValues
{
    public final SingleValue<Boolean> IS_SCREAMING = getSingle("goat_is_screaming", false);
    public final SingleValue<Boolean> HAS_LEFT_HORN = getSingle("goat_has_left_horn", true);
    public final SingleValue<Boolean> HAS_RIGHT_HORN = getSingle("goat_has_right_horn", true);

    public GoatValues()
    {
        super();

        registerSingle(IS_SCREAMING, HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }
}
