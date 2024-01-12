package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class FrogWatcher extends LivingEntityWatcher
{
    public FrogWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.FROG);
    }

    public Frog.Variant getFrogVariant()
    {
        var type = get(ValueIndex.FROG.FROG_VARIANT);

        if (type == FrogVariant.TEMPERATE)
            return Frog.Variant.TEMPERATE;
        else if (type == FrogVariant.COLD)
            return Frog.Variant.COLD;
        else if (type == FrogVariant.WARM)
            return Frog.Variant.WARM;

        logger.warn("No suitable Variant for FrogVariant '%s'".formatted(type));
        return Frog.Variant.TEMPERATE;
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.FROG);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("variant"))
        {
            var typeString = nbt.getString("variant");
            ResourceLocation rl = BuiltInRegistries.FROG_VARIANT.getKey(FrogVariant.TEMPERATE);
            FrogVariant type = FrogVariant.TEMPERATE;

            try
            {
                rl = new ResourceLocation(typeString);
                type = BuiltInRegistries.FROG_VARIANT.get(rl);
            }
            catch (Throwable t)
            {
                logger.error("Failed reading FrogVariant from NBT: " + t.getMessage());
            }

            write(ValueIndex.FROG.FROG_VARIANT, type);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = this.getFrogVariant().getKey().asString();
        nbt.putString("variant", variant);
    }
}
