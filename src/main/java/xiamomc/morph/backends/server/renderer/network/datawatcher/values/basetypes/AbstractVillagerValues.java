package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractVillagerValues extends AgeableMobValues
{
    public final SingleValue<Integer> HEADSHAKE_TIMER = getSingle(0);

    public AbstractVillagerValues()
    {
        super();

        registerSingle(HEADSHAKE_TIMER);
    }
}
