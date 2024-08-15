package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.HorseProperties;

public class HorseWatcher extends AbstractHorseWatcher
{
    public HorseWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.HORSE);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.HORSE);
    }

    public Horse.Color getColor()
    {
        var value = this.read(ValueIndex.HORSE.HORSE_VARIANT);
        var type = value & 255;

        return Horse.Color.values()[type];
    }

    public Horse.Style getStyle()
    {
        var value = this.read(ValueIndex.HORSE.HORSE_VARIANT);
        var type = value >> 8;

        return Horse.Style.values()[type];
    }

    //region Caches
    private Horse.Color horseColor;
    private Horse.Style horseStyle;
    //endregion Caches

    private int computeHorseVariant()
    {
        var color = horseColor == null ? Horse.Color.WHITE : horseColor;
        var style = horseStyle == null ? Horse.Style.NONE : horseStyle;

        return color.ordinal() | style.ordinal() << 8;
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(HorseProperties.class);

        if (property.equals(properties.COLOR))
        {
            this.horseColor = (Horse.Color) value;
            this.writePersistent(ValueIndex.HORSE.HORSE_VARIANT, computeHorseVariant());
        }

        if (property.equals(properties.STYLE))
        {
            this.horseStyle = (Horse.Style) value;
            this.writePersistent(ValueIndex.HORSE.HORSE_VARIANT, computeHorseVariant());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            this.writePersistent(ValueIndex.HORSE.HORSE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", read(ValueIndex.HORSE.HORSE_VARIANT));
    }
}
