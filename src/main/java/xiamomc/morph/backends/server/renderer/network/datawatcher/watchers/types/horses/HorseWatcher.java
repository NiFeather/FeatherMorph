package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

import java.util.Arrays;
import java.util.Random;

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
        var value = this.get(ValueIndex.HORSE.HORSE_VARIANT);
        var type = value & 255;

        return Horse.Color.values()[type];
    }

    public Horse.Style getStyle()
    {
        var value = this.get(ValueIndex.HORSE.HORSE_VARIANT);
        var type = value >> 8;

        return Horse.Style.values()[type];
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();

        //https://zh.minecraft.wiki/w/%E9%A9%AC#%E6%95%B0%E6%8D%AE%E5%80%BC
        var availableColors = Arrays.stream(Horse.Color.values()).toList();
        var color = availableColors.get(random.nextInt(availableColors.size()));

        var availableStyles = Arrays.stream(Horse.Style.values()).toList();
        var style = availableStyles.get(random.nextInt(availableStyles.size()));

        var finalValue = color.ordinal() | style.ordinal() << 8;
        this.write(ValueIndex.HORSE.HORSE_VARIANT, finalValue);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
            this.write(ValueIndex.HORSE.HORSE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", get(ValueIndex.HORSE.HORSE_VARIANT));
    }
}
