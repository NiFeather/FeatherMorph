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
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
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
        var value = get(ValueIndex.CAT.CAT_VARIANT);
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
            write(ValueIndex.CAT.CAT_VARIANT, bukkitTypeToNmsHolder(variant));
        }
        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.LAY_START -> this.write(ValueIndex.CAT.IS_LYING, true);
                case AnimationNames.SIT -> this.write(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x01);
                case AnimationNames.STANDUP ->
                {
                    if (this.getOr(ValueIndex.CAT.IS_LYING, false))
                        this.write(ValueIndex.CAT.IS_LYING, false);

                    if ((this.getOr(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00) & 1) != 0)
                        this.write(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00);
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
