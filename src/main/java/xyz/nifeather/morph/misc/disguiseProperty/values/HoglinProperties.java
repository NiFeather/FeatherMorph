package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class HoglinProperties extends BaseLivingEntityProperties<Hoglin>
{
    public final SingleProperty<Boolean> IS_BABY = getSingle("hoglin/is_baby", false)
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
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(IS_BABY.id()))
            return Pair.of(IS_BABY, Boolean.valueOf(value));

        return super.parseSingleInput(key, value);
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
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                IS_BABY.id(), propertyHandler.get(IS_BABY).toString().toLowerCase()
        );
    }
}
