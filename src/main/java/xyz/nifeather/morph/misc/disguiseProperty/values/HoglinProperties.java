package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class HoglinProperties extends BaseLivingEntityProperties<Hoglin>
{
    public final SingleProperty<Boolean> IS_BABY = getSingle(PropertyNames.HOGLIN_IS_BABY, false, InputHandles::readBooleanRelaxed)
            .withValidInput("true", "false");

    public HoglinProperties()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Hoglin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Hoglin hoglin ? hoglin : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Hoglin targetEntity)
    {
        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(IS_BABY.id(), propertyHandler.get(IS_BABY).toString().toLowerCase());
    }
}
