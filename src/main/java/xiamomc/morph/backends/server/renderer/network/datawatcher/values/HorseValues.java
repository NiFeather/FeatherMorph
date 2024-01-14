package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractHorseValues;

public class HorseValues extends AbstractHorseValues
{
    public final SingleValue<Integer> HORSE_VARIANT = getSingle("horse_variant", 0);

    public HorseValues()
    {
        super();

        registerSingle(HORSE_VARIANT);
    }
}
