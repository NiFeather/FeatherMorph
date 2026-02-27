package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.OutputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public abstract class EntityPropertyCollection<E extends Entity> extends PropertyCollection<E>
{
    public final SingleProperty<Float> STATIC_YAW = SingleProperty.builder(PropertyNames.ENTITY_STATIC_YAW, 0f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    public final SingleProperty<Float> STATIC_PITCH = SingleProperty.builder(PropertyNames.ENTITY_STATIC_PITCH, 0f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    public EntityPropertyCollection()
    {
        registerSingle(STATIC_YAW, STATIC_PITCH);
    }
}
