package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractHorseValues;

public class HorseValues extends AbstractHorseValues
{
    public final SingleValue<Integer> HORSE_VARIANT = createSingle("horse_variant", 0);

    public HorseValues()
    {
        super();

        registerSingle(HORSE_VARIANT);
    }
}
