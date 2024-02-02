package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.CatVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class CatValues extends TameableAnimalValues
{
    public final SingleValue<CatVariant> CAT_VARIANT = getSingle("cat_variant", BuiltInRegistries.CAT_VARIANT.getOrThrow(CatVariant.TABBY));
    public final SingleValue<Boolean> IS_LYING = getSingle("cat_is_lying", false);
    public final SingleValue<Boolean> RELAXED = getSingle("cat_relaxed", false);
    public final SingleValue<Byte> COLLAR_COLOR = getSingle("cat_collar_color", (byte)14);

    public CatValues()
    {
        super();

        registerSingle(CAT_VARIANT, IS_LYING, RELAXED, COLLAR_COLOR);
    }
}
