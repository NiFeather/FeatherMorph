package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Creaking;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class CreakingPropertyCollection extends BaseLivingEntityPropertyCollection<Creaking>
{
    public final SingleProperty<Boolean> EYES_GLOWING = SingleProperty.builder(PropertyNames.CREAKING_EYES_GLOWING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public CreakingPropertyCollection()
    {
        registerSingle(EYES_GLOWING);
    }

    @Override
    protected @Nullable Creaking tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof Creaking creaking ? creaking : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
