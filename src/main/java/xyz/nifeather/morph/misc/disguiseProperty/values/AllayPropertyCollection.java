package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class AllayPropertyCollection extends BaseLivingEntityPropertyCollection<Allay>
{
    public final SingleProperty<Boolean> DANCING = SingleProperty.builder(PropertyNames.ALLAY_DANCING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public AllayPropertyCollection()
    {
        registerSingle(DANCING);
    }

    @Override
    protected @Nullable Allay tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof Allay allay ? allay : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
