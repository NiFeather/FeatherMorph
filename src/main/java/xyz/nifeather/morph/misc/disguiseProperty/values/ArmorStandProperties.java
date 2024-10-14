package xyz.nifeather.morph.misc.disguiseProperty.values;

import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class ArmorStandProperties extends AbstractProperties
{
    public final SingleProperty<Boolean> SHOW_ARMS = getSingle("show_arms", false);

    public ArmorStandProperties()
    {
        registerSingle(SHOW_ARMS);
    }
}
