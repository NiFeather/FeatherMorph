package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.animal.sniffer.Sniffer;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class SnifferValues extends AnimalValues
{
    public final SingleValue<Sniffer.State> SNIFFER_STATE = createSingle("sniffer_state", Sniffer.State.IDLING);
    public final SingleValue<Integer> DROP_SEED_AT_TICK = createSingle("drop_at_tick", 0);

    public SnifferValues()
    {
        registerSingle(SNIFFER_STATE, DROP_SEED_AT_TICK);
    }
}
