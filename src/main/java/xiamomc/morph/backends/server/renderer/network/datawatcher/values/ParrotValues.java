package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.TameableAnimalValues;

public class ParrotValues extends TameableAnimalValues
{
    public final SingleValue<Integer> PARROT_VARIANT = createSingle("parrot_variant", 0).withRandom(0, 1, 2, 3, 4);

    public ParrotValues()
    {
        super();

        registerSingle(PARROT_VARIANT);
    }
}
