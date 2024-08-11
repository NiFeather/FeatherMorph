package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;
import xiamomc.morph.backends.server.renderer.utilties.HolderUtils;

public class CatValues extends TameableAnimalValues
{
    public final SingleValue<Integer> CAT_VARIANT = getSingle("cat_variant", getCatVariant(CatVariant.TABBY), EntityDataTypes.CAT_VARIANT);
    public final SingleValue<Boolean> IS_LYING = getSingle("cat_is_lying", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> RELAXED = getSingle("cat_relaxed", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle("cat_collar_color", 14, EntityDataTypes.INT);

    private int getCatVariant(ResourceKey<CatVariant> key)
    {
        var cbWorld =(CraftWorld) Bukkit.getWorlds().get(0);
        var catVariantRegistry = cbWorld.getHandle().registryAccess().registry(Registries.CAT_VARIANT).get();
        var holder = HolderUtils.getHolderFor(key, Registries.CAT_VARIANT);

        return catVariantRegistry.getId(holder.value());
    }

    public CatValues()
    {
        super();

        registerSingle(CAT_VARIANT, IS_LYING, RELAXED, COLLAR_COLOR);
    }
}
