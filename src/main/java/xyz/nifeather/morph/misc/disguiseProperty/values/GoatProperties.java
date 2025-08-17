package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;

public class GoatProperties extends BaseLivingEntityProperties<Goat>
{
    public final SingleProperty<Boolean> HAS_LEFT_HORN = getSingle("goat/has_left_horn", true)
            .withRandom(true, true, true, false)
            .withValidInput("false", "true");

    public final SingleProperty<Boolean> HAS_RIGHT_HORN = getSingle("goat/has_right_horn", true)
            .withRandom(true, true, true, false)
            .withValidInput("false", "true");

    public GoatProperties()
    {
        registerSingle(HAS_LEFT_HORN, HAS_RIGHT_HORN);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        return switch (key)
        {
            case "goat/has_left_horn" -> Pair.of(HAS_LEFT_HORN, Boolean.valueOf(value));
            case "goat/has_right_horn" -> Pair.of(HAS_RIGHT_HORN, Boolean.valueOf(value));

            default -> super.parseSingleInput(key, value);
        };
    }

    @Override
    protected @Nullable Goat tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Goat goat ? goat : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Goat goat)
    {
        propertyHandler.set(HAS_LEFT_HORN, goat.hasLeftHorn());
        propertyHandler.set(HAS_RIGHT_HORN, goat.hasRightHorn());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(HAS_RIGHT_HORN, DisguiseUtils.pick(HAS_RIGHT_HORN.getRandomValues()));
        propertyHandler.set(HAS_LEFT_HORN, DisguiseUtils.pick(HAS_LEFT_HORN.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(HAS_LEFT_HORN.id(), propertyHandler.get(HAS_LEFT_HORN).toString().toLowerCase());
        map.put(HAS_RIGHT_HORN.id(), propertyHandler.get(HAS_RIGHT_HORN).toString().toLowerCase());
    }
}
