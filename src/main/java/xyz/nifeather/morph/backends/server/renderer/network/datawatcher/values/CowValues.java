package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import org.bukkit.entity.Cow;
import xyz.nifeather.morph.backends.server.renderer.network.CustomSerializeMethods;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class CowValues extends AnimalValues
{
    public final SingleValue<Cow.Variant> COW_VARIANT;

    public CowValues()
    {
        COW_VARIANT = createSingle("cow_variant", Cow.Variant.TEMPERATE);
        COW_VARIANT.setSerializeMethod(CustomSerializeMethods.COW_VARIANT);

        registerSingle(COW_VARIANT);
    }
}
