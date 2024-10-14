package xyz.nifeather.morph.misc.disguiseProperty.values;

import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class CreeperProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> CHARGED = getSingle("charged", false).withRandom(false, false, false, true);

    public CreeperProperties()
    {
        registerSingle(CHARGED);
    }
}
