package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.FrogVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class FrogValues extends AnimalValues
{
    public final SingleValue<ResourceKey<FrogVariant>> FROG_VARIANT = getSingle("frog_variant", FrogVariant.TEMPERATE).withRandom(FrogVariant.TEMPERATE, FrogVariant.COLD, FrogVariant.WARM);

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
