package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ParrotValues extends TameableAnimalValues
{
    public final SingleValue<Integer> PARROT_VARIANT = getSingle(0).withRandom(0, 1, 2, 3, 4);

    public ParrotValues()
    {
        super();

        registerSingle(PARROT_VARIANT);
    }
}
