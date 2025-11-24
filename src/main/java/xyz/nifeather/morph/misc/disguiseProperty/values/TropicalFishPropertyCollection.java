package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TropicalFishPropertyCollection extends BaseLivingEntityPropertyCollection<TropicalFish>
{
    public final SingleProperty<DyeColor> BODY_COLOR;

    public final SingleProperty<DyeColor> PATTERN_COLOR;

    public final SingleProperty<TropicalFish.Pattern> PATTERN;

    private Optional<TropicalFish.Pattern> readPattern(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(TropicalFish.Pattern.values(), propertyName, string);
    }

    public TropicalFishPropertyCollection()
    {
        List<String> dyeColorInputs = Arrays.stream(DyeColor.values())
                .map(c -> c.name().toLowerCase())
                .toList();

        BODY_COLOR = SingleProperty.builder(PropertyNames.TROPICAL_FISH_BODY_COLOR, DyeColor.GREEN)
                .withInputHandle(InputHandles::readDyeColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(DyeColor.values())
                .withSuggestions(dyeColorInputs)
                .build();

        PATTERN_COLOR = SingleProperty.builder(PropertyNames.TROPICAL_FISH_PATTERN_COLOR, DyeColor.BLACK)
                .withInputHandle(InputHandles::readDyeColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(DyeColor.values())
                .withSuggestions(dyeColorInputs)
                .build();

        PATTERN = SingleProperty.builder(PropertyNames.TROPICAL_FISH_PATTERN, TropicalFish.Pattern.BLOCKFISH)
                .withInputHandle(this::readPattern)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(TropicalFish.Pattern.values())
                .withSuggestions(Arrays.stream(TropicalFish.Pattern.values()).map(p -> p.name().toLowerCase()).toList())
                .build();

        registerSingle(BODY_COLOR, PATTERN_COLOR, PATTERN);
    }

    @Override
    protected @Nullable TropicalFish tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof TropicalFish tropicalFish ? tropicalFish : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull TropicalFish targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

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
        var bodyColor = DisguiseUtils.pick(BODY_COLOR.randomValues());
        var patternColor = DisguiseUtils.pick(PATTERN_COLOR.randomValues());
        var pattern = DisguiseUtils.pick(PATTERN.randomValues());

        propertyHandler.set(BODY_COLOR, bodyColor);
        propertyHandler.set(PATTERN_COLOR, patternColor);
        propertyHandler.set(PATTERN, pattern);
    }

}
