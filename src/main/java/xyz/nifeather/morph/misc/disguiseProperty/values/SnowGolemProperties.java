package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class SnowGolemProperties extends BaseLivingEntityProperties<Snowman>
{
    public final SingleProperty<Boolean> HAS_PUMPKIN = getSingle(PropertyNames.SNOW_GOLEM_HAS_PUMPKIN, true, InputHandles::readBooleanRelaxed)
            .withValidInput("true", "false");

    public SnowGolemProperties()
    {
        registerSingle(HAS_PUMPKIN);
    }

    @Override
    protected @Nullable Snowman tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Snowman snowman ? snowman : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Snowman targetEntity)
    {
        propertyHandler.set(HAS_PUMPKIN, !targetEntity.isDerp());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(HAS_PUMPKIN.id(), propertyHandler.get(HAS_PUMPKIN).toString().toLowerCase());
    }
}
