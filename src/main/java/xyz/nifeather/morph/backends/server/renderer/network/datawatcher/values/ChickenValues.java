package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import org.bukkit.entity.Chicken;
import xyz.nifeather.morph.backends.server.renderer.network.CustomSerializeMethods;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class ChickenValues extends AnimalValues
{
    public final SingleValue<Chicken.Variant> CHICKEN_VARIANT;

    public ChickenValues()
    {
        CHICKEN_VARIANT = createSingle("chicken_variant", Chicken.Variant.TEMPERATE);
        CHICKEN_VARIANT.setSerializeMethod(CustomSerializeMethods.CHICKEN_VARIANT);

        registerSingle(CHICKEN_VARIANT);
    }
}
