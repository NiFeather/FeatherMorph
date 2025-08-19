package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TropicalFishProperties extends BaseLivingEntityProperties<TropicalFish>
{
    public final SingleProperty<DyeColor> BODY_COLOR = getSingle(PropertyNames.TROPICAL_FISH_BODY_COLOR, DyeColor.GREEN)
            .withRandom(DyeColor.values());
    public final SingleProperty<DyeColor> PATTERN_COLOR = getSingle(PropertyNames.TROPICAL_FISH_PATTERN_COLOR, DyeColor.BLACK)
            .withRandom(DyeColor.values());
    public final SingleProperty<TropicalFish.Pattern> PATTERN = getSingle(PropertyNames.TROPICAL_FISH_PATTERN, TropicalFish.Pattern.BLOCKFISH)
            .withRandom(TropicalFish.Pattern.values());

    public TropicalFishProperties()
    {
        List<String> dyeColorInputs = Arrays.stream(DyeColor.values())
                .map(c -> c.name().toLowerCase())
                .toList();

        BODY_COLOR.withValidInput(dyeColorInputs);
        PATTERN_COLOR.withValidInput(dyeColorInputs);
        PATTERN.withValidInput(Arrays.stream(TropicalFish.Pattern.values()).map(p -> p.name().toLowerCase()).toList());

        registerSingle(BODY_COLOR, PATTERN_COLOR, PATTERN);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        return switch (key)
        {
            case PropertyNames.TROPICAL_FISH_BODY_COLOR ->
            {
                var match = Arrays.stream(DyeColor.values())
                        .filter(c -> c.name().equalsIgnoreCase(value))
                        .findFirst().orElse(null);

                if (match == null) yield null;

                yield  Pair.of(BODY_COLOR, match);
            }

            case PropertyNames.TROPICAL_FISH_PATTERN_COLOR ->
            {
                var match = Arrays.stream(DyeColor.values())
                        .filter(c -> c.name().equalsIgnoreCase(value))
                        .findFirst().orElse(null);

                if (match == null) yield null;

                yield Pair.of(PATTERN_COLOR, match);
            }

            case PropertyNames.TROPICAL_FISH_PATTERN ->
            {
                var match = Arrays.stream(TropicalFish.Pattern.values())
                        .filter(p -> p.name().equalsIgnoreCase(value))
                        .findFirst().orElse(null);

                if (match == null) yield null;

                yield Pair.of(PATTERN, match);
            }

            default -> super.parseSingleInput(key, value);
        };
    }

    @Override
    protected @Nullable TropicalFish tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof TropicalFish tropicalFish ? tropicalFish : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull TropicalFish targetEntity)
    {
        var bodyColor = targetEntity.getBodyColor();
        var patternColor = targetEntity.getPatternColor();
        var pattern = targetEntity.getPattern();

        propertyHandler.set(PATTERN_COLOR, patternColor);
        propertyHandler.set(BODY_COLOR, bodyColor);
        propertyHandler.set(PATTERN, pattern);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        var bodyColor = DisguiseUtils.pick(BODY_COLOR.getRandomValues());
        var patternColor = DisguiseUtils.pick(PATTERN_COLOR.getRandomValues());
        var pattern = DisguiseUtils.pick(PATTERN.getRandomValues());

        propertyHandler.set(BODY_COLOR, bodyColor);
        propertyHandler.set(PATTERN_COLOR, patternColor);
        propertyHandler.set(PATTERN, pattern);
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);

        map.put(BODY_COLOR.id(), propertyHandler.get(BODY_COLOR).name().toLowerCase());
        map.put(PATTERN_COLOR.id(), propertyHandler.get(PATTERN_COLOR).name().toLowerCase());
        map.put(PATTERN.id(), propertyHandler.get(PATTERN).name().toLowerCase());
    }
}
