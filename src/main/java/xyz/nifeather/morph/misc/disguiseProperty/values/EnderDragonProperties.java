package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class EnderDragonProperties extends AbstractProperties
{
    public final SingleProperty<Integer> DRAGON_PHASE = getSingle("dragon_phase", 10);

    public EnderDragonProperties()
    {
        registerSingle(DRAGON_PHASE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(DRAGON_PHASE.id()))
            return Pair.of(DRAGON_PHASE, Math.clamp(Integer.parseInt(value), 0, 10));

        return null;
    }
}
