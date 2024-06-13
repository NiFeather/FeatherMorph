package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

import java.util.Arrays;
import java.util.Random;

public class CatWatcher extends TameableAnimalWatcher
{
    public CatWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CAT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CAT);
    }

    public Cat.Type getCatType()
    {
        var value = get(ValueIndex.CAT.CAT_VARIANT);
        var index = BuiltInRegistries.CAT_VARIANT.getId(holderToNmsVariant(value));

        return Cat.Type.values()[index];
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();
        var availableVariants = Arrays.stream(Cat.Type.values()).toList();
        var targetIndex = random.nextInt(availableVariants.size());
        var targetValue = bukkitTypeToNmsHolder(availableVariants.get(targetIndex));

        this.write(ValueIndex.CAT.CAT_VARIANT, targetValue);
    }

    private CatVariant holderToNmsVariant(Holder<CatVariant> holder)
    {
        var keyOptional = holder.unwrapKey();
        if (keyOptional.isEmpty())
            throw new NullPointerException("Null ResourceKey for holder " + holder);

        return BuiltInRegistries.CAT_VARIANT.get(keyOptional.get());
    }

    private Holder<CatVariant> bukkitTypeToNmsHolder(Cat.Type bukkitType)
    {
        var bukkitKey = bukkitType.getKey();
        ResourceLocation key = new ResourceLocation(bukkitKey.namespace(), bukkitKey.getKey());

        var variant = BuiltInRegistries.CAT_VARIANT.getHolder(key);
        if (variant.isEmpty() || !variant.get().isBound())
        {
            logger.warn("Bukkit type '%s' is not in the registries, returning Tabby...".formatted(bukkitType));

            var world = ((CraftWorld) Bukkit.getWorlds().stream().findFirst().get()).getHandle();

            return world.registryAccess().registryOrThrow(Registries.CAT_VARIANT).getHolderOrThrow(CatVariant.TABBY);
        }

        return variant.get();
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("variant"))
        {
            var name = nbt.getString("variant");
            var match = Arrays.stream(Cat.Type.values())
                    .filter(t -> t.key().asString().equalsIgnoreCase(name))
                    .findFirst();

            match.ifPresent(type ->
            {
                var finalValue = bukkitTypeToNmsHolder(type);
                this.write(ValueIndex.CAT.CAT_VARIANT, finalValue);
            });
        }

        if (nbt.contains("CollarColor"))
            write(ValueIndex.CAT.COLLAR_COLOR, (int)nbt.getByte("CollarColor"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = this.getCatType().getKey().asString();
        nbt.putString("variant", variant);

        var collarColor = get(ValueIndex.CAT.COLLAR_COLOR);
        nbt.putInt("CollarColor", collarColor);
    }
}
