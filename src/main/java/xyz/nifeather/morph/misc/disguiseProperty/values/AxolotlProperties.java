package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Axolotl;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class AxolotlProperties extends AbstractProperties
{
    public final SingleProperty<Axolotl.Variant> VARIANT = getSingle("axolotl_color", Axolotl.Variant.LUCY)
            .withRandom(Axolotl.Variant.values());

    public AxolotlProperties()
    {
        registerSingle(VARIANT);
    }
}
