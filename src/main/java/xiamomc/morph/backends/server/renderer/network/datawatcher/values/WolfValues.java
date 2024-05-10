package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.animal.WolfVariant;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<WolfVariant> WOLF_VARIANT = getSingle("wolf_variant", WolfVariant);
    public final SingleValue<Boolean> BEGGING = getSingle("wolf_begging", false);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle("wolf_collar_color", 14);
    public final SingleValue<Integer> ANGER_TIME = getSingle("wolf_anger_time", 0);

    public WolfValues()
    {
        super();

        registerSingle(BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
