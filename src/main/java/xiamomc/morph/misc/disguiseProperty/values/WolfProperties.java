package xiamomc.morph.misc.disguiseProperty.values;

import com.google.errorprone.annotations.Var;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class WolfProperties extends AbstractProperties
{
    public final SingleProperty<Wolf.Variant> VARIANT = getSingle("wolf_variant", Variant.PALE)
            .withRandom(
                    Variant.PALE,
                    Variant.SPOTTED,
                    Variant.SNOWY,
                    Variant.BLACK,
                    Variant.ASHEN,
                    Variant.RUSTY,
                    Variant.WOODS,
                    Variant.CHESTNUT,
                    Variant.STRIPED
            );

    public WolfProperties()
    {
        registerSingle(VARIANT);
    }
}
