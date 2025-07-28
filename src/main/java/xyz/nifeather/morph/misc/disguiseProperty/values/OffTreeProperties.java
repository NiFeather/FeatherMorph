package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.UUID;

public class OffTreeProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> IS_BABY = getSingle("is_baby", false).withValidInput("true", "false");
    public final SingleProperty<UUID> VIRTUAL_ENTITY_UUID = getSingle("virtual_entity_uuid", UUID.randomUUID());

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(IS_BABY.id()))
            return Pair.of(IS_BABY, Boolean.valueOf(value));

        return null;
    }
}
