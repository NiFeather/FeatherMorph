package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class HappyGhastProperties extends BaseLivingEntityProperties<HappyGhast>
{
    public final SingleProperty<Boolean> IS_GHASTLING = getSingle("happy_ghast_is_ghastling", false)
            .withValidInput("true", "false");

    public HappyGhastProperties()
    {
        registerSingle(IS_GHASTLING);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(IS_GHASTLING.id()))
            return Pair.of(IS_GHASTLING, Boolean.parseBoolean(value));

        return super.parseSingleInput(key, value);
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
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "is_ghastling", propertyHandler.get(IS_GHASTLING).toString().toLowerCase()
        );
    }
}
