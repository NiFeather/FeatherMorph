package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class WolfValues extends TameableAnimalValues
{
    public final SingleValue<Boolean> BEGGING = getSingle(false);
    public final SingleValue<Integer> COLLAR_COLOR = getSingle(14);
    public final SingleValue<Integer> ANGER_TIME = getSingle(0);

    public WolfValues()
    {
        super();

        registerSingle(BEGGING, COLLAR_COLOR, ANGER_TIME);
    }
}
