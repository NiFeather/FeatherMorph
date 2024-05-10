package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.FrogVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class FrogValues extends AnimalValues
{
    public final SingleValue<FrogVariant> FROG_VARIANT = getSingle("frog_variant", getFrogVariant(FrogVariant.TEMPERATE))
            .withRandom(getFrogVariant(FrogVariant.TEMPERATE), getFrogVariant(FrogVariant.COLD), getFrogVariant(FrogVariant.WARM));

    private FrogVariant getFrogVariant(ResourceKey<FrogVariant> key)
    {
        var variant = BuiltInRegistries.FROG_VARIANT.get(key);
        if (variant == null)
        {
            logger.warn("Null FrogVariant for key: " + key.toString());
            return BuiltInRegistries.FROG_VARIANT.byId(0);
        }

        return variant;
    }

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
