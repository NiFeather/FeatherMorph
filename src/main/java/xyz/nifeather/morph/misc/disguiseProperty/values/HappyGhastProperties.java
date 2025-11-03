package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class HappyGhastProperties extends BaseLivingEntityProperties<HappyGhast>
{
    public final SingleProperty<Boolean> IS_GHASTLING = createProperty(PropertyNames.HAPPY_GHAST_IS_GHASTLING, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public HappyGhastProperties()
    {
        registerSingle(IS_GHASTLING);
    }

    @Override
    protected @Nullable HappyGhast tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof HappyGhast happyGhast ? happyGhast : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull HappyGhast happyGhast)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, happyGhast);

        propertyHandler.set(IS_GHASTLING, !happyGhast.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(IS_GHASTLING, false);
    }

}
