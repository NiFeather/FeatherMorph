package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Cat;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class CatProperties extends AbstractProperties
{
    public final SingleProperty<Cat.Type> CAT_VARIANT = getSingle("cat_variant", Cat.Type.TABBY);

    public CatProperties()
    {
        registerSingle(
                CAT_VARIANT
        );
    }
}
