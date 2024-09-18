package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.BasePiglinValues;

public class PiglinValues extends BasePiglinValues
{
    public final SingleValue<Boolean> IS_BABY = createSingle("piglin_is_baby", false);
    public final SingleValue<Boolean> CHARGING_CROSSBOW = createSingle("piglin_charging_crossbow", false);
    public final SingleValue<Boolean> DANCING = createSingle("piglin_dancing", false);

    public PiglinValues()
    {
        super();

        registerSingle(IS_BABY, CHARGING_CROSSBOW, DANCING);
    }
}
