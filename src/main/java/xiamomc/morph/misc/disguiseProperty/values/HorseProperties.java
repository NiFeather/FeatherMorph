package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Horse;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class HorseProperties extends AbstractProperties
{
    public final SingleProperty<Horse.Color> COLOR = getSingle("horse_color", Horse.Color.WHITE)
            .withRandom(Horse.Color.values());

    public final SingleProperty<Horse.Style> STYLE = getSingle("horse_style", Horse.Style.NONE)
            .withRandom(Horse.Style.values());

    public HorseProperties()
    {
        registerSingle(COLOR, STYLE);
    }
}
