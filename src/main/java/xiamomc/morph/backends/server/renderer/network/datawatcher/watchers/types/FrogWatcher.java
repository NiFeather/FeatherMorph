package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.FrogVariant;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.FrogProperties;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

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

    public Holder<FrogVariant> getFrogVariant(Frog.Variant bukkitVariant)
    {
        var bukkitKey = bukkitVariant.getKey();

        var world = ((CraftWorld) Bukkit.getWorlds().stream().findFirst().get()).getHandle();
        var registry = world.registryAccess().registryOrThrow(Registries.FROG_VARIANT);

        var holder = registry.getHolder(ResourceLocation.parse(bukkitKey.asString()));
        if (holder.isPresent())
            return holder.get();
        else
            throw new NullDependencyException("Null frog variant for id '%s'('%s')".formatted(bukkitVariant, bukkitVariant));
    }

    public Frog.Variant getBukkitFrogVariant()
    {
        var type = read(ValueIndex.FROG.FROG_VARIANT);

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

            writePersistent(ValueIndex.FROG.FROG_VARIANT, getFrogVariant(type));
        }
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(FrogProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var variant = (Frog.Variant) value;
            writePersistent(ValueIndex.FROG.FROG_VARIANT, getFrogVariant(variant));
        }
    }

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.EAT -> this.writePersistent(ValueIndex.FROG.POSE, Pose.USING_TONGUE);
                case AnimationNames.RESET -> this.remove(ValueIndex.FROG.POSE);
            }
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
