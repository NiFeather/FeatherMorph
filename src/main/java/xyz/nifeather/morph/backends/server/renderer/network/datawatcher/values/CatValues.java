package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.cat.CatVariant;
import com.github.retrooper.packetevents.protocol.entity.cat.CatVariants;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class CatValues extends TameableAnimalValues
{
    public final SingleValue<CatVariant> CAT_VARIANT = createSingle("cat_variant", CatVariants.TABBY, EntityDataTypes.TYPED_CAT_VARIANT);
    public final SingleValue<Boolean> IS_LYING = createSingle("cat_is_lying", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> RELAXED = createSingle("cat_relaxed", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> COLLAR_COLOR = createSingle("cat_collar_color", 14, EntityDataTypes.INT);

    public CatValues()
    {
        super();

        registerSingle(CAT_VARIANT, IS_LYING, RELAXED, COLLAR_COLOR);
    }
}
