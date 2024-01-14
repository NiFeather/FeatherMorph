package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.animal.FrogVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class FrogValues extends AnimalValues
{
    public final SingleValue<FrogVariant> FROG_VARIANT = getSingle("frog_variant", FrogVariant.TEMPERATE).withRandom(FrogVariant.TEMPERATE, FrogVariant.COLD, FrogVariant.WARM);

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
