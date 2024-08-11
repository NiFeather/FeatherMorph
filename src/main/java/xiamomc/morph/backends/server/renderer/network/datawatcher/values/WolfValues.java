package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
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
import xiamomc.morph.backends.server.renderer.utilties.HolderUtils;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = getSingle("wolf_begging", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle("wolf_collar_color", 14, EntityDataTypes.INT);
    public final SingleValue<Integer> ANGER_TIME = getSingle("wolf_anger_time", 0, EntityDataTypes.INT);
    public final SingleValue<Integer> WOLF_VARIANT = getSingle("wolf_variant", getWolfVariant(WolfVariants.PALE), EntityDataTypes.WOLF_VARIANT).withRandom(allVariants());

    private Integer[] allVariants;

    private Integer[] allVariants()
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

    public static Integer[] toVariantHolders(ResourceKey<WolfVariant>... resKeys)
    {
        var list = new ObjectArrayList<Integer>();

        for (ResourceKey<WolfVariant> resKey : resKeys)
            list.add(getWolfVariant(resKey));

        return list.toArray(new Integer[]{});
    }

    public static int getWolfVariant(ResourceKey<WolfVariant> resKey)
    {
        var cbWorld =(CraftWorld) Bukkit.getWorlds().get(0);
        var registry = cbWorld.getHandle().registryAccess().registry(Registries.WOLF_VARIANT).get();
        var holder = HolderUtils.getHolderFor(resKey, Registries.WOLF_VARIANT);

        return registry.getId(holder.value());
    }

    public WolfValues()
    {
        super();

        registerSingle(WOLF_VARIANT, BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
