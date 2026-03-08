package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.AbstractNautilus;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public abstract class AbstractNautilusPropertyCollection<N extends AbstractNautilus> extends BaseLivingEntityPropertyCollection<N>
{
    public final SingleProperty<Boolean> DASHING = SingleProperty.builder(PropertyNames.NAUTILUS_DASHING, false)
            .withInputHandle(InputHandles::reservedException)
            .withOutputHandle(OutputHandles::writeBoolean)
            .hideFromUserInput(true)
            .build();

    public AbstractNautilusPropertyCollection()
    {
        registerSingle(DASHING);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
