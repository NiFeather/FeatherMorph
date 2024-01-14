package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.BasePiglinValues;

public class PiglinValues extends BasePiglinValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle("piglin_is_baby", false);
    public final SingleValue<Boolean> CHARGING_CROSSBOW = getSingle("piglin_charging_crossbow", false);
    public final SingleValue<Boolean> DANCING = getSingle("piglin_dancing", false);

    public PiglinValues()
    {
        super();

        registerSingle(IS_BABY, CHARGING_CROSSBOW, DANCING);
    }
}
