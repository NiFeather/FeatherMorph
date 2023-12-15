package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.animal.FrogVariant;

public class FrogValues extends AnimalValues
{
    public final SingleValue<FrogVariant> FROG_VARIANT = getSingle(FrogVariant.TEMPERATE).withRandom(FrogVariant.TEMPERATE, FrogVariant.COLD, FrogVariant.WARM);

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
