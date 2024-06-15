package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
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

    private Holder<FrogVariant> getFrogVariant(ResourceKey<FrogVariant> key)
    {
        var world = ((CraftWorld) Bukkit.getWorlds().stream().findFirst().get()).getHandle();

        return world.registryAccess().registryOrThrow(Registries.FROG_VARIANT).getHolderOrThrow(key);
    }

    public Frog.Variant getBukkitFrogVariant()
    {
        var type = get(ValueIndex.FROG.FROG_VARIANT);

        var keyOptional = type.unwrapKey();
        if (keyOptional.isEmpty())
        {
            logger.warn("Empty key for value '%s'?!".formatted(type));
            return Frog.Variant.TEMPERATE;
        }

        var key = keyOptional.get().location().toString();
        for (var val : Frog.Variant.values())
        {
            if (val.getKey().asString().equals(key))
                return val;
        }

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
            ResourceLocation rl;
            ResourceKey<FrogVariant> type = FrogVariant.TEMPERATE;

            try
            {
                rl = ResourceLocation.parse(typeString);
                type = ResourceKey.create(Registries.FROG_VARIANT, rl);
            }
            catch (Throwable t)
            {
                logger.error("Failed reading FrogVariant from NBT: " + t.getMessage());
            }

            write(ValueIndex.FROG.FROG_VARIANT, getFrogVariant(type));
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = this.getBukkitFrogVariant().getKey().asString();
        nbt.putString("variant", variant);
    }
}
