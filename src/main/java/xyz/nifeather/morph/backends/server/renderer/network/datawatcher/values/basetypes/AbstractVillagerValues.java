package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractVillagerValues extends AgeableMobValues
{
    public final SingleValue<Integer> HEADSHAKE_TIMER = createSingle("ab_villager_handshake_timer", 0);

    public AbstractVillagerValues()
    {
        super();

        registerSingle(HEADSHAKE_TIMER);
    }
}
