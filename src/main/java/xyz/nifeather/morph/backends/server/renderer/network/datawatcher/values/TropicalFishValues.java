package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractFishValues;

public class TropicalFishValues extends AbstractFishValues
{
    public final SingleValue<Integer> FISH_VARIANT = createSingle("tropical_variant", 0);

    public TropicalFishValues()
    {
        super();

        registerSingle(FISH_VARIANT);
    }
}
