package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.MushroomCow;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class MooshroomProperties extends AbstractProperties
{
    public final SingleProperty<MushroomCow.Variant> VARIANT = getSingle("mooshroom_variant", MushroomCow.Variant.RED)
            .withRandom(MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.BROWN);

    public MooshroomProperties()
    {
        registerSingle(VARIANT);
    }
}
