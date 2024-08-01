package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Fox;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class FoxProperties extends AbstractProperties
{
    public final SingleProperty<Fox.Type> VARIANT = getSingle("fox_variant", Fox.Type.RED)
            .withRandom(Fox.Type.values());

    public FoxProperties()
    {
        registerSingle(VARIANT);
    }
}
