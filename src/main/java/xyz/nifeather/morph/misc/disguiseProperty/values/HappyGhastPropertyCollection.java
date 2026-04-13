package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class HappyGhastPropertyCollection extends BaseLivingEntityPropertyCollection<HappyGhast>
{
    public final SingleProperty<Boolean> IS_GHASTLING = SingleProperty.builder(PropertyNames.HAPPY_GHAST_IS_GHASTLING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public HappyGhastPropertyCollection()
    {
        registerSingle(IS_GHASTLING);
    }

    @Override
    protected @Nullable HappyGhast tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof HappyGhast happyGhast ? happyGhast : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull HappyGhast happyGhast)
    {
        super.setupPropertiesFromEntity(propertyHandler, happyGhast);

        propertyHandler.set(IS_GHASTLING, !happyGhast.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(IS_GHASTLING, false);
    }

}
