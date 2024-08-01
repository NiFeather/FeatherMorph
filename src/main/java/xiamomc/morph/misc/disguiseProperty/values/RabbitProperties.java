package xiamomc.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;

public class RabbitProperties extends AbstractProperties
{
    public final SingleProperty<Rabbit.Type> VARIANT = getSingle("rabbit_type", Type.BROWN)
            .withRandom(Type.values());

    public RabbitProperties()
    {
        registerSingle(VARIANT);
    }
}
