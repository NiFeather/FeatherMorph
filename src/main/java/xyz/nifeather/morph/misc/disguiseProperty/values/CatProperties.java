package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Registry;
import org.bukkit.entity.Cat;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class CatProperties extends AbstractProperties
{
    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle("cat_variant", Cat.Type.TABBY)
            .withRandom(
                    Registry.CAT_VARIANT.stream().toList()
            );

    public CatProperties()
    {
        registerSingle(
                CAT_VARIANT
        );
    }
}
