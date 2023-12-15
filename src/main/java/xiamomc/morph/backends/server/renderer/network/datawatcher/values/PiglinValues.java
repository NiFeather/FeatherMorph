package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.BasePiglinValues;

public class PiglinValues extends BasePiglinValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle(false);
    public final SingleValue<Boolean> CHARGING_CROSSBOW = getSingle(false);
    public final SingleValue<Boolean> DANCING = getSingle(false);

    public PiglinValues()
    {
        super();

        registerSingle(IS_BABY, CHARGING_CROSSBOW, DANCING);
    }
}
