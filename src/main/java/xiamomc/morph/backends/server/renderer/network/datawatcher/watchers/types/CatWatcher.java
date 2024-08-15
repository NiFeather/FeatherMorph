package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.CatProperties;

import java.util.Arrays;

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
        var value = read(ValueIndex.CAT.CAT_VARIANT);
        var key = value.unwrapKey().orElse(null);
        if (key == null)
            logger.warn("Null Key for holder " + value);

        return Registry.CAT_VARIANT.get(Key.key(key.location().toString()));
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
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(bukkitKey.namespace(), bukkitKey.getKey());

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
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(CatProperties.class);

        if (property.equals(properties.CAT_VARIANT))
        {
            var variant = (Cat.Type) value;
            writePersistent(ValueIndex.CAT.CAT_VARIANT, bukkitTypeToNmsHolder(variant));
        }
        super.onPropertyWrite(property, value);
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
                case AnimationNames.LAY_START -> this.writePersistent(ValueIndex.CAT.IS_LYING, true);
                case AnimationNames.SIT -> this.writePersistent(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x01);
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    if (this.readOr(ValueIndex.CAT.IS_LYING, false))
                        this.writePersistent(ValueIndex.CAT.IS_LYING, false);

                    if ((this.readOr(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00) & 1) != 0)
                        this.writePersistent(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00);
                }
            }
        }
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
                this.writePersistent(ValueIndex.CAT.CAT_VARIANT, finalValue);
            });
        }

        if (nbt.contains("CollarColor"))
            writePersistent(ValueIndex.CAT.COLLAR_COLOR, (int)nbt.getByte("CollarColor"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = this.getCatType().getKey().asString();
        nbt.putString("variant", variant);

        var collarColor = read(ValueIndex.CAT.COLLAR_COLOR);
        nbt.putInt("CollarColor", collarColor);
    }
}
