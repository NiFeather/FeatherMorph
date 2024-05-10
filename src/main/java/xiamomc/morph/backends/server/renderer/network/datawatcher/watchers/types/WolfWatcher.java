package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.WolfValues;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class WolfWatcher extends TameableAnimalWatcher
{
    public WolfWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.WOLF);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.WOLF);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("CollarColor"))
            write(ValueIndex.WOLF.COLLAR_COLOR, (int)nbt.getByte("CollarColor"));

        if (nbt.contains("variant"))
        {
            var typeString = nbt.getString("variant");
            ResourceLocation rl;
            ResourceKey<WolfVariant> type = WolfVariants.PALE;

            try
            {
                rl = new ResourceLocation(typeString);
                type = ResourceKey.create(Registries.WOLF_VARIANT, rl);
            }
            catch (Throwable t)
            {
                logger.error("Failed reading FrogVariant from NBT: " + t.getMessage());
            }

            write(ValueIndex.WOLF.WOLF_VARIANT, getVariant(type));
        }
    }

    private Holder<WolfVariant> getVariant(ResourceKey<WolfVariant> key)
    {
        var world = ((CraftWorld) Bukkit.getWorlds().stream().findFirst().get()).getHandle();
        var registry = world.registryAccess().registryOrThrow(Registries.WOLF_VARIANT);

        var holder = registry.getHolder(key);
        if (holder.isEmpty())
        {
            logger.warn("No suitable Holder for wolf variant " + key);
            return ValueIndex.WOLF.WOLF_VARIANT.defaultValue();
        }

        return holder.get();
    }

    private Wolf.Variant getBukkitVariant(Holder<WolfVariant> holder)
    {
        var variants = new Wolf.Variant[]
        {
                Wolf.Variant.WOODS,
                Wolf.Variant.ASHEN,
                Wolf.Variant.BLACK,
                Wolf.Variant.PALE,
                Wolf.Variant.RUSTY,
                Wolf.Variant.CHESTNUT,
                Wolf.Variant.SNOWY,
                Wolf.Variant.SPOTTED,
                Wolf.Variant.STRIPED
        };

        var idOptional = holder.unwrapKey();
        if (idOptional.isEmpty())
        {
            logger.error("Empty ID for holder " + holder + ", can't get bukkit variant");
            return Wolf.Variant.PALE;
        }

        var id = holder.unwrapKey().get().location().toString();
        for (Wolf.Variant variant : variants)
        {
            if (variant.key().toString().equalsIgnoreCase(id))
                return variant;
        }

        logger.error("No suitable bukkit variant for id " + id);
        return Wolf.Variant.PALE;
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("CollarColor", get(ValueIndex.WOLF.COLLAR_COLOR).byteValue());
        nbt.putString("variant", getBukkitVariant(get(ValueIndex.WOLF.WOLF_VARIANT)).key().asString());
    }
}
