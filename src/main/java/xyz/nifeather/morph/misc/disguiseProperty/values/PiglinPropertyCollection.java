package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Piglin;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class PiglinPropertyCollection extends BaseLivingEntityPropertyCollection<Piglin>
{
    public final SingleProperty<Boolean> DANCING = SingleProperty.builder(PropertyNames.PIGLIN_DANCING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public PiglinPropertyCollection()
    {
        registerSingle(DANCING);
    }

    @Override
    protected @Nullable Piglin tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof Piglin piglin ? piglin : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
