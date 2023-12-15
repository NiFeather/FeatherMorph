package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.MobValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AgeableMobValues extends MobValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle(false);

    public AgeableMobValues()
    {
        super();

        registerSingle(IS_BABY);
    }
}
