package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.FrogVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;
import xiamomc.morph.backends.server.renderer.utilties.HolderUtils;

public class FrogValues extends AnimalValues
{
    public final SingleValue<Holder<FrogVariant>> FROG_VARIANT = getSingle("frog_variant", getFrogVariant(FrogVariant.TEMPERATE))
            .withRandom(getFrogVariant(FrogVariant.TEMPERATE), getFrogVariant(FrogVariant.COLD), getFrogVariant(FrogVariant.WARM));

    private Holder<FrogVariant> getFrogVariant(ResourceKey<FrogVariant> key)
    {
        return HolderUtils.getHolderFor(key, Registries.FROG_VARIANT);
    }

    public FrogValues()
    {
        super();

        var handle = WrappedDataWatcher.Registry.fromHandle(EntityDataSerializers.FROG_VARIANT);
        FROG_VARIANT.setSerializer(handle);

        registerSingle(FROG_VARIANT);
    }
}
