package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.FrogVariant;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;
import xiamomc.morph.backends.server.renderer.utilties.HolderUtils;

public class FrogValues extends AnimalValues
{
    public final SingleValue<Integer> FROG_VARIANT = getSingle("frog_variant", getFrogVariant(FrogVariant.TEMPERATE), EntityDataTypes.FROG_VARIANT)
            .withRandom(getFrogVariant(FrogVariant.TEMPERATE), getFrogVariant(FrogVariant.COLD), getFrogVariant(FrogVariant.WARM));

    private int getFrogVariant(ResourceKey<FrogVariant> key)
    {
        var registryAccess = ((CraftWorld)Bukkit.getWorlds().get(0)).getHandle().registryAccess().registry(Registries.FROG_VARIANT);
        var holder = HolderUtils.getHolderFor(key, Registries.FROG_VARIANT);

        return registryAccess.get().getId(holder.value());
    }

    public FrogValues()
    {
        super();

        registerSingle(FROG_VARIANT);
    }
}
