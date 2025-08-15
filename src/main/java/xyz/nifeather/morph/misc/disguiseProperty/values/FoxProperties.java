package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;

public class FoxProperties extends BaseLivingEntityProperties<Fox>
{
    public final SingleProperty<Fox.Type> VARIANT = getSingle("fox_variant", Fox.Type.RED)
            .withRandom(Fox.Type.values())
            .withValidInput("default", "snow");

    public FoxProperties()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(VARIANT.id()))
        {
            var type = value.equals("default") ? Fox.Type.RED : Fox.Type.SNOW;

            return Pair.of(VARIANT, type);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Fox tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Fox fox ? fox : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Fox fox)
    {
        propertyHandler.set(VARIANT, fox.getFoxType());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "variant", propertyHandler.get(VARIANT).name().toLowerCase()
        );
    }
}
