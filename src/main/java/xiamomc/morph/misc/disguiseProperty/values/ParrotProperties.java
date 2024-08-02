package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class ParrotProperties extends AbstractProperties
{
    public final SingleProperty<Parrot.Variant> VARIANT = getSingle("parrot_variant", Variant.RED)
            .withRandom(Variant.values());

    public ParrotProperties()
    {
        registerSingle(VARIANT);
    }
}
