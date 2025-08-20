package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class HappyGhastProperties extends BaseLivingEntityProperties<HappyGhast>
{
    public final SingleProperty<Boolean> IS_GHASTLING = getSingle(PropertyNames.HAPPY_GHAST_IS_GHASTLING, false, InputHandles::readBooleanRelaxed)
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
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull HappyGhast happyGhast)
    {
        propertyHandler.set(IS_GHASTLING, !happyGhast.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(IS_GHASTLING, false);
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(IS_GHASTLING.id(), propertyHandler.get(IS_GHASTLING).toString().toLowerCase());
    }
}
