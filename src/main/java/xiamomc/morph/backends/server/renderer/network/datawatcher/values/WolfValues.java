package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = getSingle("wolf_begging", false);
    public final SingleValue<Byte> COLLAR_COLOR = getSingle("wolf_collar_color", (byte)14);
    public final SingleValue<Integer> ANGER_TIME = getSingle("wolf_anger_time", 0);

    public WolfValues()
    {
        super();

        registerSingle(BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
