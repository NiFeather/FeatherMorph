package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.SheepPropertyCollection;

public class SheepWatcher extends LivingEntityWatcher
{
    public SheepWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SHEEP);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SHEEP);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var sheepProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(SheepPropertyCollection.class);

        if (property.equals(sheepProperties.DYE_COLOR))
        {
            var dyeColor = (DyeColor) value;
            this.writePersistent(ValueIndex.SHEEP.WOOL_TYPE, dyeColor.getWoolData());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("Color", read(ValueIndex.SHEEP.WOOL_TYPE));
    }
}
