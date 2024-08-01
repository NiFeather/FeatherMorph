package xiamomc.morph.misc.disguiseProperty.values;

import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class GoatProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> HAS_LEFT_HORN = getSingle("has_left_horn", true)
            .withRandom(true, true, true, false);

    public final SingleProperty<Boolean> HAS_RIGHT_HORN = getSingle("has_right_horn", true)
            .withRandom(true, true, true, false);

    public GoatProperties()
    {
        registerSingle(HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }
}
