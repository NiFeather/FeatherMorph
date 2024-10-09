package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;
import xyz.nifeather.morph.backends.server.renderer.utilties.HolderUtils;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = createSingle("wolf_begging", false);
    public final SingleValue<Integer> COLLAR_COLOR = createSingle("wolf_collar_color", 14);
    public final SingleValue<Integer> ANGER_TIME = createSingle("wolf_anger_time", 0);
    public final SingleValue<Holder<WolfVariant>> WOLF_VARIANT = createSingle("wolf_variant", getWolfVariant(WolfVariants.PALE)).withRandom(allVariants());

    @Nullable
    private Holder<WolfVariant>[] allVariants;

    private Holder<WolfVariant>[] allVariants()
    {
        if (this.allVariants != null)
            return allVariants;

        this.allVariants = toVariantHolders(
            WolfVariants.PALE,
            WolfVariants.SPOTTED,
            WolfVariants.SNOWY,
            WolfVariants.BLACK,
            WolfVariants.ASHEN,
            WolfVariants.RUSTY,
            WolfVariants.WOODS,
            WolfVariants.CHESTNUT,
            WolfVariants.STRIPED);

        return this.allVariants;
    }

    public static Holder<WolfVariant>[] toVariantHolders(ResourceKey<WolfVariant>... resKeys)
    {
        var list = new ObjectArrayList<Holder<WolfVariant>>();

        for (ResourceKey<WolfVariant> resKey : resKeys)
            list.add(getWolfVariant(resKey));

        return list.toArray(new Holder[]{});
    }

    public static Holder<WolfVariant> getWolfVariant(ResourceKey<WolfVariant> resKey)
    {
        return HolderUtils.getHolderFor(resKey, Registries.WOLF_VARIANT);
    }

    public WolfValues()
    {
        super();

        var handle = WrappedDataWatcher.Registry.fromHandle(EntityDataSerializers.WOLF_VARIANT);
        WOLF_VARIANT.setSerializer(handle);

        registerSingle(WOLF_VARIANT, BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
