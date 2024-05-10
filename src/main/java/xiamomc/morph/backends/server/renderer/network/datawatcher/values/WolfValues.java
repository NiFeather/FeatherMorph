package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = getSingle("wolf_begging", false);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle("wolf_collar_color", 14);
    public final SingleValue<Integer> ANGER_TIME = getSingle("wolf_anger_time", 0);
    public final SingleValue<Holder<WolfVariant>> WOLF_VARIANT = getSingle("wolf_variant", getWolfVariant(WolfVariants.PALE)).withRandom(allVariants());

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
        var world = ((CraftWorld)Bukkit.getWorlds().stream().findFirst().get()).getHandle();

        return world.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).getHolderOrThrow(resKey);
    }

    public WolfValues()
    {
        super();

        var handle = WrappedDataWatcher.Registry.fromHandle(EntityDataSerializers.WOLF_VARIANT);
        WOLF_VARIANT.setSerializer(handle);

        registerSingle(WOLF_VARIANT, BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
