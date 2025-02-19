package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import org.bukkit.entity.Cat;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class CatValues extends TameableAnimalValues
{
    public final SingleValue<Integer> CAT_VARIANT = createSingle("cat_variant", Cat.Type.TABBY, EntityDataTypes.CAT_VARIANT);
    public final SingleValue<Boolean> IS_LYING = createSingle("cat_is_lying", false);
    public final SingleValue<Boolean> RELAXED = createSingle("cat_relaxed", false);
    public final SingleValue<Integer> COLLAR_COLOR = createSingle("cat_collar_color", 14);

    public CatValues()
    {
        super();

        CAT_VARIANT.setSerializeMethod(CustomSerializeMethods.CAT_VARIANT);
        registerSingle(CAT_VARIANT, IS_LYING, RELAXED, COLLAR_COLOR);
    }
}
