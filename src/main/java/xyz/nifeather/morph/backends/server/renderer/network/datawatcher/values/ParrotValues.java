package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class ParrotValues extends TameableAnimalValues
{
    public final SingleValue<Integer> PARROT_VARIANT = createSingle("parrot_variant", 0);

    public ParrotValues()
    {
        super();

        registerSingle(PARROT_VARIANT);
    }
}
