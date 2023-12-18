package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

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
        var index = BuiltInRegistries.CAT_VARIANT.getId(value);

        return Cat.Type.values()[index];
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();
        var availableVariants = Arrays.stream(Cat.Type.values()).toList();
        var targetIndex = random.nextInt(availableVariants.size());
        var targetValue = bukkitTypeToNms(availableVariants.get(targetIndex));

        this.write(ValueIndex.CAT.CAT_VARIANT, targetValue);
    }

    private CatVariant bukkitTypeToNms(Cat.Type bukkitType)
    {
        var bukkitKey = bukkitType.getKey();
        ResourceLocation key = new ResourceLocation(bukkitKey.namespace(), bukkitKey.getKey());
        return BuiltInRegistries.CAT_VARIANT.get(key);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("variant"))
        {
            var name = nbt.getString("variant");
            var match = Arrays.stream(Cat.Type.values())
                    .filter(t -> t.name().equalsIgnoreCase(name))
                    .findFirst();

            match.ifPresent(type ->
            {
                var finalValue = bukkitTypeToNms(type);
                this.write(ValueIndex.CAT.CAT_VARIANT, finalValue);
            });
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = this.getCatType().getKey().asString();
        nbt.putString("variant", variant);
    }
}
