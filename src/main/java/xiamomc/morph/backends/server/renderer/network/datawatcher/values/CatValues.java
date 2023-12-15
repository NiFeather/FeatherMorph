package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.entity.Cat;

public class CatValues extends TameableAnimalValues
{
    public final SingleValue<CatVariant> CAT_VARIANT = getSingle(BuiltInRegistries.CAT_VARIANT.getOrThrow(CatVariant.TABBY));
    public final SingleValue<Boolean> IS_LYING = getSingle(false);
    public final SingleValue<Boolean> RELAXED = getSingle(false);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle(14);

    public CatValues()
    {
        super();

        registerSingle(CAT_VARIANT, IS_LYING, RELAXED, COLLAR_COLOR);
    }
}
