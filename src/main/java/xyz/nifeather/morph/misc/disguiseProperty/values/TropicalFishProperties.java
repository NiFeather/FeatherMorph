package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TropicalFishProperties extends BaseLivingEntityProperties<TropicalFish>
{
    public final SingleProperty<DyeColor> BODY_COLOR = getSingle("tropical_fish/body_color", DyeColor.GREEN)
            .withRandom(DyeColor.values());
    public final SingleProperty<DyeColor> PATTERN_COLOR = getSingle("tropical_fish/pattern_color", DyeColor.BLACK)
            .withRandom(DyeColor.values());
    public final SingleProperty<TropicalFish.Pattern> PATTERN = getSingle("tropical_fish/pattern", TropicalFish.Pattern.BLOCKFISH)
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
        logger.info("Input! key is %s and value is %s".formatted(key, value));
        if (key.equals(BODY_COLOR.id()))
        {
            var match = Arrays.stream(DyeColor.values())
                    .filter(c -> c.name().equalsIgnoreCase(value))
                    .findFirst().orElse(null);

            logger.info("BODY COILOR get " + match);
            if (match == null) return null;

            return Pair.of(BODY_COLOR, match);
        }
        else if (key.equals(PATTERN_COLOR.id()))
        {
            var match = Arrays.stream(DyeColor.values())
                    .filter(c -> c.name().equalsIgnoreCase(value))
                    .findFirst().orElse(null);

            if (match == null) return null;

            return Pair.of(PATTERN_COLOR, match);
        }
        else if (key.equals(PATTERN.id()))
        {
            var match = Arrays.stream(TropicalFish.Pattern.values())
                    .filter(p -> p.name().equalsIgnoreCase(value))
                    .findFirst().orElse(null);

            if (match == null) return null;

            return Pair.of(PATTERN, match);
        }

        return super.parseSingleInput(key, value);
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
