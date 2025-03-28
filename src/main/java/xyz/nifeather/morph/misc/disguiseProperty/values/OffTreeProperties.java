package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class OffTreeProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> IS_BABY = getSingle("is_baby", false).withValidInput("true", "false");

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(IS_BABY.id()))
            return Pair.of(IS_BABY, Boolean.valueOf(value));

        return null;
    }
}
