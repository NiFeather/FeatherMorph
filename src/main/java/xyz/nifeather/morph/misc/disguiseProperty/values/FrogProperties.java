package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Frog;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class FrogProperties extends AbstractProperties
{
    public final SingleProperty<Frog.Variant> VARIANT = getSingle("frog_variant", Frog.Variant.TEMPERATE)
            .withRandom(Frog.Variant.TEMPERATE, Frog.Variant.COLD, Frog.Variant.WARM);

    public FrogProperties()
    {
        registerSingle(
                VARIANT
        );
    }
}
