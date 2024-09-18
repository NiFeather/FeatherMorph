package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class AbstractHorseValues extends AgeableMobValues
{
    public final SingleValue<Byte> FLAGS = createSingle("ab_horse_flags", (byte)0);

    public AbstractHorseValues()
    {
        super();

        registerSingle(FLAGS);
    }
}
