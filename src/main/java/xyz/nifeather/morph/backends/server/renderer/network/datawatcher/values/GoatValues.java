package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class GoatValues extends AnimalValues
{
    public final SingleValue<Boolean> IS_SCREAMING = createSingle("goat_is_screaming", false);
    public final SingleValue<Boolean> HAS_LEFT_HORN = createSingle("goat_has_left_horn", true);
    public final SingleValue<Boolean> HAS_RIGHT_HORN = createSingle("goat_has_right_horn", true);

    public GoatValues()
    {
        super();

        registerSingle(IS_SCREAMING, HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }
}
