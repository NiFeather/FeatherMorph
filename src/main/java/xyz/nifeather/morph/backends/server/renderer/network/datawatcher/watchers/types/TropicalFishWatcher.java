package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.entity.CraftTropicalFish;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.TropicalFishPropertyCollection;

public class TropicalFishWatcher extends LivingEntityWatcher
{
    public TropicalFishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.TROPICAL_FISH);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TROPICAL);
    }

    private volatile DyeColor baseColor = DyeColor.BLACK;
    private volatile DyeColor patternColor = DyeColor.BLACK;
    private volatile TropicalFish.Pattern pattern = TropicalFish.Pattern.BLOCKFISH;

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var tropicalProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(TropicalFishPropertyCollection.class);

        if (property.equals(tropicalProperties.BODY_COLOR))
        {
            baseColor = (DyeColor) value;
            updatePackedData();
        }
        else if (property.equals(tropicalProperties.PATTERN_COLOR))
        {
            patternColor = (DyeColor) value;
            updatePackedData();
        }
        else if (property.equals(tropicalProperties.PATTERN))
        {
            pattern = (TropicalFish.Pattern) value;
            updatePackedData();
        }

        super.onPropertyWrite(property, value);
    }

    private void updatePackedData()
    {
        var data = CraftTropicalFish.getData(patternColor, baseColor, pattern);
        this.writePersistent(ValueIndex.TROPICAL.FISH_VARIANT, data);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", read(ValueIndex.TROPICAL.FISH_VARIANT));
    }
}
