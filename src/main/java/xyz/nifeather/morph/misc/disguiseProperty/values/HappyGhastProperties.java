package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class HappyGhastProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> IS_GHASTLING = getSingle("happy_ghast_is_ghastling", false)
            .withValidInput("true", "false");

    public HappyGhastProperties()
    {
        registerSingle(IS_GHASTLING);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(IS_GHASTLING.id()))
            return Pair.of(IS_GHASTLING, Boolean.parseBoolean(value));

        return null;
    }
}
