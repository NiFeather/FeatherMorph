package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class LlamaProperties extends AbstractProperties
{
    public final SingleProperty<Llama.Color> VARIANT = getSingle("llama_variant", Color.CREAMY)
            .withRandom(Color.values());

    public LlamaProperties()
    {
        registerSingle(VARIANT);
    }
}
